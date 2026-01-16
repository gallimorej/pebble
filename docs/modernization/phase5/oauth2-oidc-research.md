# OAuth 2.0 / OpenID Connect Implementation Research

**Project:** Pebble Blog Migration to Spring Security 6.2.2 & Spring Framework 6.1.14
**Phase:** 5 - OAuth 2.0 Integration
**Date:** 2026-01-15
**Status:** Research Phase

## Executive Summary

This document provides comprehensive research findings for implementing OAuth 2.0 and OpenID Connect (OIDC) authentication in Pebble using Spring Security 6.2.2 and Spring Framework 6.1.14. The implementation will complement the existing form-based authentication system, allowing users to log in using external identity providers like Google and GitHub.

## Table of Contents

1. [Current Authentication Architecture](#current-authentication-architecture)
2. [Spring Security OAuth2 Client Dependencies](#spring-security-oauth2-client-dependencies)
3. [Google OAuth 2.0 Provider Setup](#google-oauth-20-provider-setup)
4. [GitHub OAuth 2.0 Provider Setup](#github-oauth-20-provider-setup)
5. [OAuth Callback Handling](#oauth-callback-handling)
6. [Security Considerations](#security-considerations)
7. [Integration with Existing Authentication](#integration-with-existing-authentication)
8. [Implementation Recommendations](#implementation-recommendations)

---

## 1. Current Authentication Architecture

### 1.1 Existing Security Components

Pebble currently implements a custom authentication system with the following components:

#### Core Classes
- **`DefaultUserDetailsService`** - Loads user details from `SecurityRealm`
- **`PebbleAuthenticationProvider`** - Custom authentication provider with password upgrade support
- **`PebblePasswordEncoder`** - Handles BCrypt/SHA-1 password encoding with automatic upgrade
- **`DefaultSecurityRealm`** - Manages user storage and retrieval
- **`PebbleUserDetails`** - User details implementation with blog-specific roles

#### Security Configuration (`applicationContext-security.xml`)
- Form-based authentication at `/j_spring_security_check`
- Remember-me authentication support
- Basic authentication for XML feeds
- Custom redirect strategy with URL sanitization
- Private blog access control

#### Authentication Flow
1. User submits credentials via login form
2. `UsernamePasswordAuthenticationFilter` processes request
3. `PebbleAuthenticationProvider` authenticates against `SecurityRealm`
4. On success, redirects to saved request or default URL
5. On failure, redirects to `/loginPage.action?error=login.incorrect`

### 1.2 Legacy OpenID 2.0 Support

Previous versions of Pebble included OpenID 2.0 support which was removed during Spring Security 6 migration:
- **Removed:** `spring-security-openid` dependency (deprecated in Spring Security 5, removed in 6)
- **Evidence:** `OpenIdAuthenticationFailureHandler` and `OpenIdUserDetailsService` classes still present
- **Comment in pom.xml:** "OpenID 2.0 support has been deprecated and removed"

### 1.3 Existing Redirect Strategy

`PebbleRedirectStrategy` implements important security:
```java
public static String sanitiseUrl(String contextPath, String url) {
    URI uri = URI.create(url);
    if (uri.getRawAuthority() != null) {
        // Removes authority section to prevent open redirects
        // Returns only path + query
    }
    return contextPath + url;
}
```

This prevents open redirect vulnerabilities, critical for OAuth callback handling.

---

## 2. Spring Security OAuth2 Client Dependencies

### 2.1 Required Maven Dependencies

Add to `pom.xml`:

```xml
<!-- Spring Security OAuth2 Client -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-client</artifactId>
    <version>${spring-security.version}</version>
</dependency>

<!-- Spring Security OAuth2 Jose (JWT/JWS support for OIDC) -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
    <version>${spring-security.version}</version>
</dependency>
```

**Note:** These dependencies are compatible with Spring Security 6.2.2 and Spring Framework 6.1.14.

### 2.2 Dependency Overview

| Artifact | Purpose | Key Classes |
|----------|---------|-------------|
| `spring-security-oauth2-client` | OAuth2 client support, authorization code flow | `OAuth2AuthorizedClient`, `ClientRegistration` |
| `spring-security-oauth2-jose` | JWT/JWS/JWK support for ID token validation | `JwtDecoder`, `NimbusJwtDecoder` |

### 2.3 Transitive Dependencies

Spring Security OAuth2 automatically includes:
- **Nimbus JOSE+JWT** - JWT parsing and validation
- **OAuth2 OIDC SDK** - OpenID Connect support
- **Spring Security OAuth2 Core** - Core OAuth2 abstractions

---

## 3. Google OAuth 2.0 Provider Setup

### 3.1 Google Cloud Console Configuration

#### Step 1: Create OAuth 2.0 Credentials
1. Navigate to [Google Cloud Console](https://console.cloud.google.com/)
2. Select or create a project
3. Go to **APIs & Services > Credentials**
4. Click **Create Credentials > OAuth client ID**
5. Choose **Web application**

#### Step 2: Configure Authorized Redirect URIs

**Development:**
```
http://localhost:8080/login/oauth2/code/google
```

**Production:**
```
https://yourdomain.com/login/oauth2/code/google
```

**Template:** `{baseUrl}/login/oauth2/code/{registrationId}`

#### Step 3: Obtain Credentials
- **Client ID:** `xxxxxx.apps.googleusercontent.com`
- **Client Secret:** `GOCSPX-xxxxxxxxxxxxx`

### 3.2 Spring Security Configuration

#### Option A: Spring Boot Application Properties

Create `src/main/resources/application.yml`:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
            # redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"  # Default
```

**Environment Variables:**
```bash
export GOOGLE_CLIENT_ID="xxxxxx.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="GOCSPX-xxxxxxxxxxxxx"
```

#### Option B: XML Configuration (Pebble's Current Approach)

Create `oauth2-client-config.xml`:

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Google OAuth2 Client Registration -->
    <bean id="googleClientRegistration"
          class="org.springframework.security.oauth2.client.registration.ClientRegistration"
          factory-method="withRegistrationId">
        <constructor-arg value="google"/>
        <property name="clientId" value="${google.client.id}"/>
        <property name="clientSecret" value="${google.client.secret}"/>
        <property name="clientAuthenticationMethod">
            <bean class="org.springframework.security.oauth2.core.ClientAuthenticationMethod"
                  factory-method="valueOf">
                <constructor-arg value="CLIENT_SECRET_BASIC"/>
            </bean>
        </property>
        <property name="authorizationGrantType">
            <bean class="org.springframework.security.oauth2.core.AuthorizationGrantType"
                  factory-method="valueOf">
                <constructor-arg value="AUTHORIZATION_CODE"/>
            </bean>
        </property>
        <property name="redirectUri" value="{baseUrl}/login/oauth2/code/{registrationId}"/>
        <property name="scope">
            <list>
                <value>openid</value>
                <value>profile</value>
                <value>email</value>
            </list>
        </property>
        <property name="authorizationUri" value="https://accounts.google.com/o/oauth2/v2/auth"/>
        <property name="tokenUri" value="https://www.googleapis.com/oauth2/v4/token"/>
        <property name="userInfoUri" value="https://www.googleapis.com/oauth2/v3/userinfo"/>
        <property name="userNameAttributeName" value="sub"/>
        <property name="jwkSetUri" value="https://www.googleapis.com/oauth2/v3/certs"/>
        <property name="clientName" value="Google"/>
    </bean>

    <!-- Client Registration Repository -->
    <bean id="clientRegistrationRepository"
          class="org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository">
        <constructor-arg>
            <list>
                <ref bean="googleClientRegistration"/>
            </list>
        </constructor-arg>
    </bean>
</beans>
```

#### Option C: Java Configuration (Recommended for Spring 6)

```java
@Configuration
public class OAuth2ClientConfig {

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(googleClientRegistration());
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
            .clientId(googleClientId)
            .clientSecret(googleClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "profile", "email")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://www.googleapis.com/oauth2/v4/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .clientName("Google")
            .build();
    }
}
```

### 3.3 Google Scopes

| Scope | Purpose | Data Returned |
|-------|---------|---------------|
| `openid` | OpenID Connect authentication | `sub` (user ID) |
| `profile` | Basic profile information | `name`, `given_name`, `family_name`, `picture` |
| `email` | Email address | `email`, `email_verified` |
| `address` | Postal address | Address components |
| `phone` | Phone number | Phone number |

**Recommended for Pebble:** `openid`, `profile`, `email`

### 3.4 Google User Attributes

Available in `OAuth2User.getAttributes()`:

```json
{
  "sub": "1234567890",
  "name": "John Doe",
  "given_name": "John",
  "family_name": "Doe",
  "picture": "https://lh3.googleusercontent.com/...",
  "email": "john.doe@example.com",
  "email_verified": true,
  "locale": "en"
}
```

---

## 4. GitHub OAuth 2.0 Provider Setup

### 4.1 GitHub OAuth App Configuration

#### Step 1: Create OAuth App
1. Navigate to [GitHub Settings > Developer Settings > OAuth Apps](https://github.com/settings/developers)
2. Click **New OAuth App**
3. Fill in application details:
   - **Application name:** Pebble Blog
   - **Homepage URL:** `http://localhost:8080` (development)
   - **Authorization callback URL:** `http://localhost:8080/login/oauth2/code/github`

#### Step 2: Obtain Credentials
- **Client ID:** `Ov23lixxxxxxxxxx`
- **Client Secret:** Generate and save securely

### 4.2 Spring Security Configuration

#### Application Properties

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope:
              - read:user
              - user:email
            # Uses CommonOAuth2Provider.GITHUB defaults
```

#### Java Configuration

```java
@Bean
public ClientRegistrationRepository clientRegistrationRepository() {
    return new InMemoryClientRegistrationRepository(
        googleClientRegistration(),
        githubClientRegistration()
    );
}

private ClientRegistration githubClientRegistration() {
    return ClientRegistration.withRegistrationId("github")
        .clientId(githubClientId)
        .clientSecret(githubClientSecret)
        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
        .scope("read:user", "user:email")
        .authorizationUri("https://github.com/login/oauth/authorize")
        .tokenUri("https://github.com/login/oauth/access_token")
        .userInfoUri("https://api.github.com/user")
        .userNameAttributeName("id")
        .clientName("GitHub")
        .build();
}
```

### 4.3 GitHub Scopes

| Scope | Purpose | Data Access |
|-------|---------|-------------|
| `read:user` | Read user profile data | Public profile information |
| `user:email` | Access email addresses | Primary and verified emails |
| `repo` | Repository access | Not needed for authentication |

**Recommended for Pebble:** `read:user`, `user:email`

### 4.4 GitHub User Attributes

Available in `OAuth2User.getAttributes()`:

```json
{
  "id": 12345678,
  "login": "johndoe",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "avatar_url": "https://avatars.githubusercontent.com/u/12345678",
  "bio": "Software Developer",
  "location": "San Francisco, CA",
  "blog": "https://johndoe.com"
}
```

**Note:** Email may be `null` if user's email is private. Fetch via API: `GET /user/emails`

---

## 5. OAuth Callback Handling

### 5.1 Authorization Flow

```
┌─────────┐                                              ┌──────────────┐
│ Browser │                                              │ OAuth Provider│
└────┬────┘                                              └──────┬───────┘
     │                                                          │
     │  1. Click "Login with Google"                          │
     ├──────────────────────────────────────►                 │
     │     GET /oauth2/authorization/google                    │
     │                                                          │
     │  2. Redirect to provider with state + PKCE             │
     ├─────────────────────────────────────────────────────►  │
     │     https://accounts.google.com/o/oauth2/v2/auth       │
     │     ?client_id=...&redirect_uri=...&state=...          │
     │                                                          │
     │  3. User authenticates and grants consent               │
     │                                                          │
     │  4. Redirect to callback with authorization code        │
     │◄─────────────────────────────────────────────────────┤
     │     GET /login/oauth2/code/google?code=...&state=...   │
     │                                                          │
     │  5. Exchange code for tokens                            │
     ├─────────────────────────────────────────────────────►  │
     │     POST /oauth2/v4/token (token exchange)              │
     │                                                          │
     │  6. Retrieve user info with access token                │
     ├─────────────────────────────────────────────────────►  │
     │     GET /oauth2/v3/userinfo                             │
     │                                                          │
     │  7. Create authenticated session                         │
     │◄──────────────────────────────────────                  │
     │     Redirect to saved request or default URL            │
     │                                                          │
```

### 5.2 Security Filter Chain Configuration

Add OAuth2 login to existing security configuration:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests((authorize) -> authorize
            .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
            .requestMatchers("/**/*.secureaction").hasAnyRole("BLOG_OWNER", "BLOG_PUBLISHER", "BLOG_CONTRIBUTOR", "BLOG_ADMIN", "BLOG_READER")
            .anyRequest().permitAll()
        )
        .formLogin((form) -> form
            .loginProcessingUrl("/j_spring_security_check")
            .loginPage("/loginPage.action")
            .successHandler(authenticationSuccessHandler())
            .failureHandler(authenticationFailureHandler())
        )
        .oauth2Login((oauth2) -> oauth2
            .loginPage("/loginPage.action")  // Custom login page with OAuth buttons
            .authorizationEndpoint((authorization) -> authorization
                .baseUri("/oauth2/authorization")  // Default
            )
            .redirectionEndpoint((redirection) -> redirection
                .baseUri("/login/oauth2/code/*")  // Default
            )
            .userInfoEndpoint((userInfo) -> userInfo
                .userService(oauth2UserService())
            )
            .successHandler(oauth2AuthenticationSuccessHandler())
            .failureHandler(oauth2AuthenticationFailureHandler())
        )
        .logout((logout) -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        );

    return http.build();
}
```

### 5.3 Custom OAuth2UserService

Map OAuth2 user to Pebble user:

```java
@Service
public class PebbleOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private SecurityRealm securityRealm;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String oauth2UserId = oauth2User.getName();  // "sub" for Google, "id" for GitHub

        try {
            // Map OAuth2 user to Pebble user
            PebbleUserDetails pebbleUser = mapOAuth2UserToPebbleUser(
                registrationId,
                oauth2UserId,
                oauth2User.getAttributes()
            );

            // Return combined user with both OAuth2 attributes and Pebble authorities
            return new PebbleOAuth2User(oauth2User, pebbleUser);

        } catch (SecurityRealmException e) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user"),
                "Unable to map OAuth2 user to Pebble user",
                e
            );
        }
    }

    private PebbleUserDetails mapOAuth2UserToPebbleUser(
            String provider,
            String providerId,
            Map<String, Object> attributes) throws SecurityRealmException {

        // Extract email based on provider
        String email = extractEmail(provider, attributes);

        // Look up existing user by OAuth2 identifier
        PebbleUserDetails user = securityRealm.getUserByOAuth2Id(provider, providerId);

        if (user == null) {
            // Try to find by email
            user = securityRealm.getUserByEmail(email);

            if (user != null) {
                // Link OAuth2 identity to existing user
                securityRealm.linkOAuth2Identity(user.getUsername(), provider, providerId);
            } else {
                // Auto-register new user (if enabled)
                user = autoRegisterUser(provider, providerId, attributes, email);
            }
        }

        return user;
    }

    private String extractEmail(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> (String) attributes.get("email");
            case "github" -> (String) attributes.get("email");  // May be null
            default -> null;
        };
    }
}
```

### 5.4 Custom Success Handler

```java
public class OAuth2AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public OAuth2AuthenticationSuccessHandler() {
        setDefaultTargetUrl("/");
        setTargetUrlParameter("redirectUrl");
        setRedirectStrategy(new PebbleRedirectStrategy());
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // Log successful OAuth2 authentication
        logger.info("OAuth2 authentication successful: " + authentication.getName());

        // Continue with standard redirect logic
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
```

### 5.5 Custom Failure Handler

```java
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public OAuth2AuthenticationFailureHandler() {
        setDefaultFailureUrl("/loginPage.action?error=oauth2.error");
        setRedirectStrategy(new PebbleRedirectStrategy());
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        // Determine error type
        String errorParam;
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            String errorCode = oauth2Exception.getError().getErrorCode();
            errorParam = "oauth2." + errorCode;
        } else if (exception instanceof UsernameNotFoundException) {
            errorParam = "oauth2.user.not.found";
        } else {
            errorParam = "oauth2.error";
        }

        logger.warn("OAuth2 authentication failed: " + exception.getMessage());

        setDefaultFailureUrl("/loginPage.action?error=" + errorParam);
        super.onAuthenticationFailure(request, response, exception);
    }
}
```

---

## 6. Security Considerations

### 6.1 CSRF Protection

Spring Security 6.2.2 enables CSRF protection by default for state-changing operations.

#### OAuth2 and CSRF
- **Authorization requests** use `state` parameter (not CSRF token)
- **Callback endpoint** (`/login/oauth2/code/*`) is automatically excluded from CSRF
- **Form login** still requires CSRF token

#### Configuration
```java
http.csrf((csrf) -> csrf
    .ignoringRequestMatchers("/oauth2/**", "/login/oauth2/**")
);
```

### 6.2 State Parameter

The `state` parameter prevents CSRF attacks during OAuth2 authorization:

#### How It Works
1. Before redirecting to provider, Spring Security generates random `state` value
2. `state` is stored in HTTP session or cookie
3. Provider includes `state` in callback redirect
4. Spring Security verifies `state` matches stored value

#### Implementation
Spring Security handles `state` automatically. No additional configuration needed.

#### Security Properties
- **Cryptographically random** - Uses `SecureRandom`
- **Single-use** - Validated once and discarded
- **Time-bound** - Expires with session

### 6.3 PKCE (Proof Key for Code Exchange)

PKCE enhances authorization code flow security, especially for public clients.

#### Spring Security 6.2.2 Support
- **Enabled by default** for all OAuth2 clients
- Automatically generates `code_verifier` and `code_challenge`
- Uses SHA-256 hashing

#### Flow
1. Generate random `code_verifier` (43-128 characters)
2. Compute `code_challenge = BASE64URL(SHA256(code_verifier))`
3. Send `code_challenge` and `code_challenge_method=S256` in authorization request
4. Provider stores `code_challenge`
5. Send `code_verifier` in token exchange
6. Provider verifies: `code_challenge == BASE64URL(SHA256(code_verifier))`

#### Disable (Not Recommended)
```java
http.oauth2Login((oauth2) -> oauth2
    .authorizationEndpoint((authorization) -> authorization
        .authorizationRequestResolver(
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository(),
                "/oauth2/authorization"
            ) {
                @Override
                public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                    OAuth2AuthorizationRequest authorizationRequest = super.resolve(request);
                    // Remove PKCE parameters (not recommended)
                    return authorizationRequest;
                }
            }
        )
    )
);
```

### 6.4 Token Storage

#### Access Token Storage
- **Default:** Stored in `OAuth2AuthorizedClientRepository`
- **Implementation:** `HttpSessionOAuth2AuthorizedClientRepository` (session-based)
- **Security:** Tokens bound to HTTP session

#### Refresh Token Handling
- Automatically used by `OAuth2AuthorizedClientManager`
- Transparently refreshes expired access tokens
- Requires `offline_access` scope (provider-dependent)

#### Best Practices
1. **Never expose tokens to client-side JavaScript**
2. **Use HttpOnly cookies** for session management
3. **Implement token revocation** on logout
4. **Short-lived access tokens** (1 hour recommended)
5. **Rotate refresh tokens** on use

### 6.5 Redirect URI Validation

#### Provider-Side Validation
- **Exact match** required by most providers (Google, GitHub)
- **No wildcards** allowed in production
- **HTTPS required** in production

#### Application-Side Validation (Pebble)

Use existing `PebbleRedirectStrategy` for post-authentication redirects:

```java
public class PebbleRedirectStrategy implements RedirectStrategy {
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) {
        response.sendRedirect(response.encodeRedirectURL(sanitiseUrl(request.getContextPath(), url)));
    }

    public static String sanitiseUrl(String contextPath, String url) {
        URI uri = URI.create(url);
        if (uri.getRawAuthority() != null) {
            // Remove authority to prevent open redirect
            StringBuilder sb = new StringBuilder();
            if (uri.getRawPath() != null) {
                sb.append(uri.getRawPath());
            }
            if (uri.getRawQuery() != null) {
                sb.append("?").append(uri.getRawQuery());
            }
            return sb.toString();
        } else {
            return contextPath + url;
        }
    }
}
```

#### Open Redirect Prevention
1. **Whitelist allowed redirect paths**
2. **Remove authority section** from URIs
3. **Validate against application context path**
4. **Reject absolute URLs** with external domains

### 6.6 ID Token Validation

For OpenID Connect (Google), ID tokens must be validated:

#### Automatic Validation
Spring Security validates:
- **Signature** - Using provider's JWK Set
- **Issuer (`iss`)** - Matches expected issuer
- **Audience (`aud`)** - Matches client ID
- **Expiration (`exp`)** - Token not expired
- **Issued At (`iat`)** - Token not issued in future

#### Custom Validation

```java
@Bean
public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory() {
    OidcIdTokenDecoderFactory idTokenDecoderFactory = new OidcIdTokenDecoderFactory();

    // Custom JWT validator
    idTokenDecoderFactory.setJwtValidatorFactory((clientRegistration) -> {
        OAuth2TokenValidator<Jwt> defaultValidator =
            new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(clientRegistration.getProviderDetails().getIssuerUri())
            );

        // Add custom validators
        return defaultValidator;
    });

    return idTokenDecoderFactory;
}
```

### 6.7 Session Management

#### Session Fixation Protection
Spring Security creates new session after authentication:

```java
http.sessionManagement((session) -> session
    .sessionFixation().migrateSession()  // Migrate session attributes
    .maximumSessions(1)  // Limit concurrent sessions
    .maxSessionsPreventsLogin(false)  // Allow new session, invalidate old
);
```

#### Logout
Invalidate both application session and OAuth2 tokens:

```java
@Bean
public LogoutSuccessHandler oidcLogoutSuccessHandler(
        ClientRegistrationRepository clientRegistrationRepository) {

    OidcClientInitiatedLogoutSuccessHandler successHandler =
        new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

    successHandler.setPostLogoutRedirectUri("{baseUrl}");

    return successHandler;
}

// Configuration
http.logout((logout) -> logout
    .logoutUrl("/logout")
    .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
    .invalidateHttpSession(true)
    .deleteCookies("JSESSIONID")
);
```

---

## 7. Integration with Existing Authentication

### 7.1 Dual Authentication Strategy

Pebble will support both form-based and OAuth2 authentication:

```
┌─────────────────────────────────────────────────┐
│           Pebble Authentication                 │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌──────────────────┐   ┌──────────────────┐   │
│  │  Form-Based      │   │  OAuth 2.0/OIDC  │   │
│  │  Authentication  │   │  Authentication  │   │
│  └────────┬─────────┘   └────────┬─────────┘   │
│           │                      │              │
│           └──────────┬───────────┘              │
│                      │                          │
│           ┌──────────▼──────────┐               │
│           │  SecurityRealm      │               │
│           │  (User Storage)     │               │
│           └─────────────────────┘               │
│                                                 │
└─────────────────────────────────────────────────┘
```

### 7.2 User Account Linking

#### Scenarios

1. **New OAuth2 User**
   - No existing Pebble account
   - Auto-register with email from OAuth2
   - Create user with `BLOG_READER` role

2. **Existing User, First OAuth2 Login**
   - Existing Pebble account with matching email
   - Link OAuth2 identity to existing account
   - Preserve existing roles and permissions

3. **Existing User, Subsequent OAuth2 Login**
   - OAuth2 identity already linked
   - Direct authentication
   - Use existing roles

4. **Multiple OAuth2 Providers**
   - User can link Google AND GitHub
   - Store provider-specific identifiers
   - Single Pebble account

#### Database Schema Extension

Add OAuth2 identity table:

```sql
CREATE TABLE oauth2_identity (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (provider, provider_user_id),
    FOREIGN KEY (username) REFERENCES pebble_user(username)
);
```

**Note:** Pebble uses file-based storage. This may need to be implemented in `SecurityRealm`:

```java
// In SecurityRealm interface
void linkOAuth2Identity(String username, String provider, String providerId)
    throws SecurityRealmException;

PebbleUserDetails getUserByOAuth2Id(String provider, String providerId)
    throws SecurityRealmException;

PebbleUserDetails getUserByEmail(String email)
    throws SecurityRealmException;
```

### 7.3 Login Page Modifications

Update `/loginPage.action` JSP to include OAuth2 buttons:

```jsp
<!-- Existing form login -->
<form action="${pageContext.request.contextPath}/j_spring_security_check" method="post">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    <input type="text" name="j_username" placeholder="Username"/>
    <input type="password" name="j_password" placeholder="Password"/>
    <button type="submit">Login</button>
</form>

<!-- OAuth2 login buttons -->
<div class="oauth2-login">
    <p>Or sign in with:</p>
    <a href="${pageContext.request.contextPath}/oauth2/authorization/google"
       class="oauth2-button google-button">
        <img src="${pageContext.request.contextPath}/images/google-icon.png" alt="Google"/>
        Sign in with Google
    </a>
    <a href="${pageContext.request.contextPath}/oauth2/authorization/github"
       class="oauth2-button github-button">
        <img src="${pageContext.request.contextPath}/images/github-icon.png" alt="GitHub"/>
        Sign in with GitHub
    </a>
</div>
```

### 7.4 Authority Mapping

Map OAuth2 scopes and attributes to Pebble roles:

```java
@Bean
public GrantedAuthoritiesMapper userAuthoritiesMapper() {
    return (authorities) -> {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        // OAuth2 users default to BLOG_READER role
        mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_BLOG_READER"));

        // Check if user has specific OAuth2 scopes that map to higher privileges
        authorities.forEach(authority -> {
            if (authority instanceof OAuth2UserAuthority oauth2Auth) {
                Map<String, Object> attributes = oauth2Auth.getAttributes();

                // Example: Admin users in OAuth2 provider get BLOG_ADMIN role
                if (isAdminUser(attributes)) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_BLOG_ADMIN"));
                }
            }
        });

        return mappedAuthorities;
    };
}
```

### 7.5 Migration Path for Existing OpenID 2.0 Users

Pebble previously supported OpenID 2.0. Migration considerations:

1. **No automatic migration** - OpenID 2.0 and OAuth 2.0 use different identifiers
2. **Email-based linking** - Match by email address
3. **User communication** - Notify users to re-authenticate with OAuth2
4. **Backward compatibility** - Keep `OpenIdAuthenticationFailureHandler` for error messages

---

## 8. Implementation Recommendations

### 8.1 Phase 5A: Foundation (Week 1-2)

#### Goals
- Add OAuth2 dependencies
- Configure Google provider
- Implement basic OAuth2 login flow

#### Tasks
1. **Update `pom.xml`**
   - Add `spring-security-oauth2-client`
   - Add `spring-security-oauth2-jose`

2. **Create OAuth2 Configuration Class**
   - `OAuth2ClientConfig.java`
   - Google `ClientRegistration` bean
   - `ClientRegistrationRepository` bean

3. **Update Security Configuration**
   - Add `.oauth2Login()` to `SecurityFilterChain`
   - Configure authorization and redirection endpoints

4. **Create Custom `OAuth2UserService`**
   - `PebbleOAuth2UserService.java`
   - Basic user mapping logic
   - Error handling

5. **Update Login Page**
   - Add "Sign in with Google" button
   - Update JSP/HTML

#### Testing
- Manual testing with Google OAuth2
- Verify authorization flow
- Check session creation
- Test logout

### 8.2 Phase 5B: User Integration (Week 3-4)

#### Goals
- Extend `SecurityRealm` for OAuth2 support
- Implement user account linking
- Add GitHub provider

#### Tasks
1. **Extend `SecurityRealm` Interface**
   ```java
   void linkOAuth2Identity(String username, String provider, String providerId);
   PebbleUserDetails getUserByOAuth2Id(String provider, String providerId);
   PebbleUserDetails getUserByEmail(String email);
   ```

2. **Implement in `DefaultSecurityRealm`**
   - File-based OAuth2 identity storage
   - XML format: `oauth2-identities.xml`
   - CRUD operations

3. **Enhance `PebbleOAuth2UserService`**
   - Check for existing OAuth2 identity
   - Link by email if found
   - Auto-register new users

4. **Add GitHub Configuration**
   - GitHub `ClientRegistration`
   - Update login page with GitHub button

5. **Authority Mapping**
   - Implement `GrantedAuthoritiesMapper`
   - Map OAuth2 users to `BLOG_READER` by default

#### Testing
- Test new user registration via OAuth2
- Test existing user linking by email
- Test multiple OAuth2 logins
- Test both Google and GitHub

### 8.3 Phase 5C: Security Hardening (Week 5)

#### Goals
- Implement custom success/failure handlers
- Add comprehensive security validations
- CSRF and redirect URI protection

#### Tasks
1. **Create Custom Handlers**
   - `OAuth2AuthenticationSuccessHandler`
   - `OAuth2AuthenticationFailureHandler`
   - Use existing `PebbleRedirectStrategy`

2. **ID Token Validation**
   - Custom `JwtDecoderFactory` if needed
   - Additional claim validation

3. **Session Management**
   - Configure session fixation protection
   - Implement logout with token revocation

4. **Security Audit**
   - Review CSRF configuration
   - Validate redirect URI handling
   - Test PKCE implementation

#### Testing
- Security testing (OWASP guidelines)
- Test open redirect prevention
- Test CSRF protection
- Test session management

### 8.4 Phase 5D: User Experience (Week 6)

#### Goals
- Improve login page UI
- Add user account management
- Error handling and messages

#### Tasks
1. **Enhanced Login Page**
   - Responsive design
   - OAuth2 provider icons
   - Loading states

2. **Account Management Page**
   - Display linked OAuth2 accounts
   - Link/unlink providers
   - Security settings

3. **Error Messages**
   - Localized error messages
   - `messages.properties` updates
   - User-friendly error pages

4. **Documentation**
   - User guide for OAuth2 login
   - Admin guide for configuration
   - Troubleshooting guide

#### Testing
- User acceptance testing
- Cross-browser testing
- Mobile responsiveness
- Accessibility testing

### 8.5 Configuration Properties

Create `oauth2.properties`:

```properties
# Google OAuth2
google.client.id=${GOOGLE_CLIENT_ID}
google.client.secret=${GOOGLE_CLIENT_SECRET}

# GitHub OAuth2
github.client.id=${GITHUB_CLIENT_ID}
github.client.secret=${GITHUB_CLIENT_SECRET}

# OAuth2 Features
oauth2.auto.registration.enabled=true
oauth2.link.by.email.enabled=true
oauth2.default.role=BLOG_READER
```

Load in Spring configuration:

```xml
<context:property-placeholder location="classpath:oauth2.properties"
                              ignore-resource-not-found="true"/>
```

### 8.6 Security Checklist

- [ ] HTTPS enforced in production
- [ ] CSRF protection enabled
- [ ] State parameter validated
- [ ] PKCE enabled (default in Spring Security 6.2.2)
- [ ] ID token signature verified
- [ ] Redirect URI validated
- [ ] Open redirect protection implemented
- [ ] Tokens stored securely (HttpSession)
- [ ] Session fixation protection enabled
- [ ] Logout invalidates OAuth2 tokens
- [ ] Error messages don't leak sensitive information
- [ ] Rate limiting on authentication endpoints
- [ ] Audit logging for OAuth2 events

### 8.7 Deployment Considerations

#### Environment Variables (Production)
```bash
export GOOGLE_CLIENT_ID="production-client-id.apps.googleusercontent.com"
export GOOGLE_CLIENT_SECRET="production-client-secret"
export GITHUB_CLIENT_ID="production-github-client-id"
export GITHUB_CLIENT_SECRET="production-github-client-secret"
```

#### Redirect URIs (Production)
- Update Google Cloud Console: `https://yourdomain.com/login/oauth2/code/google`
- Update GitHub OAuth App: `https://yourdomain.com/login/oauth2/code/github`

#### HTTPS Configuration
- Enforce HTTPS in Spring Security:
  ```java
  http.requiresChannel((channel) -> channel
      .anyRequest().requiresSecure()
  );
  ```

#### Load Balancer Considerations
If behind a load balancer/proxy, configure forwarded headers:
```properties
server.forward-headers-strategy=framework
```

---

## 9. Code Examples

### 9.1 Complete Java Configuration

```java
package net.sourceforge.pebble.security.oauth2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class OAuth2SecurityConfig {

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.client.secret}")
    private String githubClientSecret;

    @Bean
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/**/*.secureaction").hasAnyRole(
                    "BLOG_OWNER", "BLOG_PUBLISHER", "BLOG_CONTRIBUTOR", "BLOG_ADMIN", "BLOG_READER"
                )
                .anyRequest().permitAll()
            )
            .formLogin((form) -> form
                .loginProcessingUrl("/j_spring_security_check")
                .loginPage("/loginPage.action")
                .permitAll()
            )
            .oauth2Login((oauth2) -> oauth2
                .loginPage("/loginPage.action")
                .userInfoEndpoint((userInfo) -> userInfo
                    .userService(oauth2UserService())
                )
                .successHandler(oauth2AuthenticationSuccessHandler())
                .failureHandler(oauth2AuthenticationFailureHandler())
            )
            .logout((logout) -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
            );

        return http.build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            googleClientRegistration(),
            githubClientRegistration()
        );
    }

    private ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
            .clientId(googleClientId)
            .clientSecret(googleClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "profile", "email")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://www.googleapis.com/oauth2/v4/token")
            .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .clientName("Google")
            .build();
    }

    private ClientRegistration githubClientRegistration() {
        return ClientRegistration.withRegistrationId("github")
            .clientId(githubClientId)
            .clientSecret(githubClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("read:user", "user:email")
            .authorizationUri("https://github.com/login/oauth/authorize")
            .tokenUri("https://github.com/login/oauth/access_token")
            .userInfoUri("https://api.github.com/user")
            .userNameAttributeName("id")
            .clientName("GitHub")
            .build();
    }

    @Bean
    public PebbleOAuth2UserService oauth2UserService() {
        return new PebbleOAuth2UserService();
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler();
    }

    @Bean
    public OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler();
    }
}
```

### 9.2 PebbleOAuth2UserService Implementation

```java
package net.sourceforge.pebble.security.oauth2;

import net.sourceforge.pebble.security.PebbleUserDetails;
import net.sourceforge.pebble.security.SecurityRealm;
import net.sourceforge.pebble.security.SecurityRealmException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

public class PebbleOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private SecurityRealm securityRealm;

    @Value("${oauth2.auto.registration.enabled:true}")
    private boolean autoRegistrationEnabled;

    @Value("${oauth2.link.by.email.enabled:true}")
    private boolean linkByEmailEnabled;

    @Value("${oauth2.default.role:BLOG_READER}")
    private String defaultRole;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String oauth2UserId = oauth2User.getName();
        Map<String, Object> attributes = oauth2User.getAttributes();

        try {
            PebbleUserDetails pebbleUser = mapOAuth2UserToPebbleUser(
                registrationId,
                oauth2UserId,
                attributes
            );

            return new PebbleOAuth2User(oauth2User, pebbleUser);

        } catch (SecurityRealmException e) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user", "Unable to map OAuth2 user", null),
                e
            );
        }
    }

    private PebbleUserDetails mapOAuth2UserToPebbleUser(
            String provider,
            String providerId,
            Map<String, Object> attributes) throws SecurityRealmException {

        String email = extractEmail(provider, attributes);

        // 1. Check if OAuth2 identity already linked
        PebbleUserDetails user = securityRealm.getUserByOAuth2Id(provider, providerId);

        if (user != null) {
            return user;
        }

        // 2. Try to find by email and link
        if (linkByEmailEnabled && email != null) {
            user = securityRealm.getUserByEmail(email);
            if (user != null) {
                securityRealm.linkOAuth2Identity(user.getUsername(), provider, providerId);
                return user;
            }
        }

        // 3. Auto-register new user
        if (autoRegistrationEnabled) {
            return autoRegisterUser(provider, providerId, attributes, email);
        }

        throw new SecurityRealmException("User not found and auto-registration is disabled");
    }

    private PebbleUserDetails autoRegisterUser(
            String provider,
            String providerId,
            Map<String, Object> attributes,
            String email) throws SecurityRealmException {

        String name = extractName(provider, attributes);
        String username = generateUsername(provider, providerId, email);

        PebbleUserDetails newUser = new PebbleUserDetails(
            username,
            "", // No password for OAuth2 users
            name,
            email,
            null, // website
            null, // profile
            new String[]{defaultRole},
            true, // enabled
            false // authenticated via OAuth2
        );

        securityRealm.createUser(newUser);
        securityRealm.linkOAuth2Identity(username, provider, providerId);

        return newUser;
    }

    private String extractEmail(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> (String) attributes.get("email");
            case "github" -> (String) attributes.get("email");
            default -> null;
        };
    }

    private String extractName(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> (String) attributes.get("name");
            case "github" -> {
                String name = (String) attributes.get("name");
                if (name == null || name.trim().isEmpty()) {
                    name = (String) attributes.get("login");
                }
                yield name;
            }
            default -> "Unknown";
        };
    }

    private String generateUsername(String provider, String providerId, String email) {
        if (email != null) {
            String localPart = email.split("@")[0];
            return sanitizeUsername(localPart);
        }
        return provider + "_" + providerId;
    }

    private String sanitizeUsername(String username) {
        return username.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }
}
```

### 9.3 SecurityRealm Extensions

```java
// In SecurityRealm interface (add these methods)
public interface SecurityRealm {

    // Existing methods...

    /**
     * Links an OAuth2 identity to an existing Pebble user.
     */
    void linkOAuth2Identity(String username, String provider, String providerId)
        throws SecurityRealmException;

    /**
     * Retrieves a Pebble user by OAuth2 provider and provider user ID.
     */
    PebbleUserDetails getUserByOAuth2Id(String provider, String providerId)
        throws SecurityRealmException;

    /**
     * Retrieves a Pebble user by email address.
     */
    PebbleUserDetails getUserByEmail(String email)
        throws SecurityRealmException;

    /**
     * Creates a new user (for auto-registration).
     */
    void createUser(PebbleUserDetails user)
        throws SecurityRealmException;
}
```

---

## 10. Testing Strategy

### 10.1 Unit Tests

```java
@Test
public void testGoogleOAuth2UserMapping() {
    Map<String, Object> attributes = Map.of(
        "sub", "1234567890",
        "email", "john.doe@example.com",
        "name", "John Doe"
    );

    OAuth2User oauth2User = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        attributes,
        "sub"
    );

    OAuth2UserRequest userRequest = mock(OAuth2UserRequest.class);
    ClientRegistration clientRegistration = googleClientRegistration();
    when(userRequest.getClientRegistration()).thenReturn(clientRegistration);

    PebbleOAuth2UserService service = new PebbleOAuth2UserService();
    OAuth2User result = service.loadUser(userRequest);

    assertNotNull(result);
    assertEquals("john.doe@example.com", result.getAttribute("email"));
}
```

### 10.2 Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
public class OAuth2LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testOAuth2AuthorizationRedirect() throws Exception {
        mockMvc.perform(get("/oauth2/authorization/google"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("https://accounts.google.com/o/oauth2/**"));
    }

    @Test
    public void testOAuth2CallbackWithValidCode() throws Exception {
        String code = "valid-authorization-code";
        String state = "valid-state";

        mockMvc.perform(get("/login/oauth2/code/google")
                .param("code", code)
                .param("state", state))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }
}
```

### 10.3 Security Tests

Test cases:
- CSRF protection on OAuth2 endpoints
- State parameter validation
- Open redirect prevention
- Session fixation protection
- Token storage security

---

## 11. Conclusion

This research document provides a comprehensive foundation for implementing OAuth 2.0 and OpenID Connect authentication in Pebble using Spring Security 6.2.2 and Spring Framework 6.1.14. Key takeaways:

1. **Spring Security 6.2.2 provides robust OAuth2 support** with sensible defaults (PKCE, CSRF, session management)
2. **Google and GitHub are well-supported** via `CommonOAuth2Provider`
3. **Integration with existing authentication** requires extending `SecurityRealm` for OAuth2 identity management
4. **Security is paramount** - HTTPS, redirect validation, token storage must be carefully implemented
5. **User experience** can be enhanced with auto-registration and email-based account linking

### Next Steps

1. Review this document with the team
2. Approve Phase 5A implementation plan
3. Set up Google Cloud Console project
4. Configure development environment variables
5. Begin implementation with Phase 5A (Foundation)

---

## 12. References

- [Spring Security 6.2.2 OAuth 2.0 Login Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [GitHub OAuth Apps Documentation](https://docs.github.com/en/apps/oauth-apps)
- [OpenID Connect Core Specification](https://openid.net/specs/openid-connect-core-1_0.html)
- [RFC 6749 - OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749)
- [RFC 7636 - PKCE for OAuth Public Clients](https://datatracker.ietf.org/doc/html/rfc7636)
- [OWASP OAuth 2.0 Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/OAuth2_Cheat_Sheet.html)

---

**Document Version:** 1.0
**Author:** Research Agent (Claude Code)
**Last Updated:** 2026-01-15
