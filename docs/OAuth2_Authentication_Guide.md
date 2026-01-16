# OAuth 2.0 / OpenID Connect Authentication for Pebble Blog

## Overview

Pebble Blog now supports OAuth 2.0 and OpenID Connect authentication, allowing users to log in using their Google or GitHub accounts. This feature complements the existing username/password authentication system and provides a modern, secure authentication method.

## Features

- **Google OAuth 2.0 / OpenID Connect**: Login with Google accounts
- **GitHub OAuth 2.0**: Login with GitHub accounts
- **Auto-registration**: Automatically create user accounts for new OAuth2 users (configurable)
- **Backward Compatibility**: Existing form-based authentication continues to work
- **Profile Sync**: Automatically sync user profile information from OAuth2 providers
- **Security**: Built on Spring Security 6.2.2 OAuth2 framework

## Architecture

### Components

1. **OAuth2SecurityConfig.java**: Configures OAuth2 client registrations for Google and GitHub
2. **OAuth2UserService.java**: Custom user service that integrates OAuth2 authentication with Pebble's SecurityRealm
3. **OAuth2LoginController.java**: Handles OAuth2 login callbacks and user profile display
4. **applicationContext-security.xml**: Spring Security configuration with OAuth2 filter chains
5. **application-oauth2.yml**: Configuration template for OAuth2 providers

### Authentication Flow

```
┌─────────┐                ┌──────────────┐               ┌──────────────┐
│ Browser │                │ Pebble Blog  │               │ OAuth2       │
│         │                │              │               │ Provider     │
└────┬────┘                └──────┬───────┘               └──────┬───────┘
     │                            │                              │
     │  1. Click "Login with Google"                            │
     ├───────────────────────────>│                              │
     │                            │                              │
     │  2. Redirect to OAuth2 authorization URL                 │
     │<───────────────────────────┤                              │
     │                            │                              │
     │  3. Authorization request                                │
     ├──────────────────────────────────────────────────────────>│
     │                            │                              │
     │  4. User authenticates and grants permission             │
     │<──────────────────────────────────────────────────────────┤
     │                            │                              │
     │  5. Redirect to callback with authorization code         │
     ├───────────────────────────>│                              │
     │                            │  6. Exchange code for token  │
     │                            ├─────────────────────────────>│
     │                            │  7. Access token             │
     │                            │<─────────────────────────────┤
     │                            │  8. Get user info            │
     │                            ├─────────────────────────────>│
     │                            │  9. User profile             │
     │                            │<─────────────────────────────┤
     │  10. Create/update user    │                              │
     │       and establish session│                              │
     │  11. Redirect to home      │                              │
     │<───────────────────────────┤                              │
```

## Setup Instructions

### 1. Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google+ API (or Google People API)
4. Navigate to "Credentials" → "Create Credentials" → "OAuth client ID"
5. Choose "Web application"
6. Add authorized redirect URIs:
   - Development: `http://localhost:8080/login/oauth2/code/google`
   - Production: `https://yourdomain.com/login/oauth2/code/google`
7. Copy the Client ID and Client Secret

### 2. GitHub OAuth2 Setup

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click "New OAuth App"
3. Fill in the application details:
   - Application name: Your blog name
   - Homepage URL: Your blog URL
   - Authorization callback URL:
     - Development: `http://localhost:8080/login/oauth2/code/github`
     - Production: `https://yourdomain.com/login/oauth2/code/github`
4. Copy the Client ID and generate a new Client Secret

### 3. Configuration

#### Option A: Using application.yml (Recommended for Development)

1. Copy `application-oauth2.yml` to `application.yml`
2. Replace placeholders with your credentials:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-google-client-id
            client-secret: your-google-client-secret
          github:
            client-id: your-github-client-id
            client-secret: your-github-client-secret
```

3. Enable OAuth2 configuration in `applicationContext-security.xml`:
   - Uncomment the OAuth2 bean definitions
   - Ensure the filter chain includes `oauth2LoginFilter`

#### Option B: Using Environment Variables (Recommended for Production)

Set the following environment variables:

```bash
export GOOGLE_CLIENT_ID=your-google-client-id
export GOOGLE_CLIENT_SECRET=your-google-client-secret
export GITHUB_CLIENT_ID=your-github-client-id
export GITHUB_CLIENT_SECRET=your-github-client-secret
```

Update `application.yml` to use environment variables:

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
```

### 4. Enable OAuth2 in Spring Security

Edit `src/main/webapp/WEB-INF/applicationContext-security.xml` and uncomment the OAuth2 configuration sections:

1. Uncomment `clientRegistrationRepository` bean
2. Uncomment `oauth2LoginFilter` bean
3. Uncomment `oauth2AuthenticationManager` bean
4. Ensure the OAuth2 filter chain is active

## Configuration Properties

### OAuth2 User Service

```yaml
pebble:
  security:
    oauth2:
      # Auto-register new OAuth2 users
      auto-register-users: true

      # Default role for new OAuth2 users
      # Options: BLOG_READER, BLOG_CONTRIBUTOR, BLOG_PUBLISHER, BLOG_OWNER, BLOG_ADMIN
      default-role: BLOG_READER
```

### Security Configuration

- **auto-register-users**: If `true`, new OAuth2 users are automatically registered with BLOG_READER role. If `false`, only existing users can login via OAuth2.
- **default-role**: The role assigned to newly registered OAuth2 users. Administrators can grant additional roles later through the Pebble admin interface.

## User Management

### OAuth2 User Accounts

OAuth2 users are automatically created with the following username format:
- Google: `google_{user_id}` (e.g., `google_123456789`)
- GitHub: `github_{user_id}` (e.g., `github_987654321`)

### Profile Information

The following profile information is automatically synced from OAuth2 providers:

- **Name**: Full name (Google) or login/name (GitHub)
- **Email**: Email address (both providers)
- **Website**: Personal website (GitHub only)

### Role Management

1. **Initial Role**: New OAuth2 users receive the `BLOG_READER` role by default
2. **Role Escalation**: Administrators can grant additional roles through:
   - Pebble admin interface
   - Direct SecurityRealm manipulation
   - Manual user file editing (DefaultSecurityRealm)

### Disabling OAuth2 Users

To disable OAuth2 login for a user:
1. Remove the user's OAuth2 account via admin interface
2. Set `auto-register-users: false` to prevent new OAuth2 registrations

## Security Considerations

### Production Deployment

1. **HTTPS Required**: Always use HTTPS in production
   - Update redirect URIs to use HTTPS
   - Set `forceHttps: true` in authentication entry point

2. **Client Secret Protection**:
   - Never commit secrets to version control
   - Use environment variables or secret management systems
   - Rotate secrets regularly

3. **Redirect URI Validation**:
   - Ensure redirect URIs match exactly in OAuth2 provider settings
   - Use explicit URLs, not wildcards

4. **Session Management**:
   - Configure secure session cookies
   - Set appropriate session timeout
   - Enable CSRF protection

### Security Best Practices

```yaml
# Recommended production security settings
server:
  servlet:
    session:
      cookie:
        secure: true
        http-only: true
        same-site: strict
      timeout: 30m

spring:
  security:
    oauth2:
      client:
        provider:
          google:
            # Enable PKCE for enhanced security
            authorization-grant-type: authorization_code
```

## Testing

### Manual Testing

1. Start Pebble Blog: `mvn tomcat:run` or deploy to Tomcat
2. Navigate to login page: `http://localhost:8080/loginPage.action`
3. Click "Login with Google" or "Login with GitHub"
4. Complete OAuth2 authentication flow
5. Verify user is created and logged in
6. Check user profile information is synced

### Automated Testing

Create integration tests for OAuth2 authentication:

```java
@Test
public void testGoogleOAuth2Login() {
    // Test OAuth2 user service
    OAuth2UserRequest request = createMockOAuth2Request("google");
    OAuth2User user = oauth2UserService.loadUser(request);

    assertNotNull(user);
    assertTrue(user.getAuthorities().contains(new SimpleGrantedAuthority("BLOG_READER")));
}
```

## Troubleshooting

### Common Issues

1. **Redirect URI Mismatch**
   - **Error**: `redirect_uri_mismatch`
   - **Solution**: Ensure redirect URIs in OAuth2 provider settings match exactly

2. **Invalid Client ID/Secret**
   - **Error**: `invalid_client`
   - **Solution**: Verify credentials are correct and not expired

3. **User Auto-Registration Fails**
   - **Error**: User creation exception
   - **Solution**: Check SecurityRealm implementation supports user creation

4. **OAuth2 Filter Not Applied**
   - **Error**: 404 on `/login/oauth2/code/{provider}`
   - **Solution**: Verify filter chain configuration and bean definitions

### Debug Logging

Enable debug logging for OAuth2:

```yaml
logging:
  level:
    org.springframework.security.oauth2: DEBUG
    net.sourceforge.pebble.security.OAuth2UserService: DEBUG
```

## API Reference

### OAuth2SecurityConfig

```java
public static ClientRegistrationRepository clientRegistrationRepository(
    String googleClientId, String googleClientSecret,
    String githubClientId, String githubClientSecret)
```

Creates a repository of OAuth2 client registrations.

### OAuth2UserService

```java
public OAuth2User loadUser(OAuth2UserRequest userRequest)
    throws OAuth2AuthenticationException
```

Loads and processes OAuth2 user information, creating or updating Pebble users.

### OAuth2LoginController

```java
public View successCallback(HttpServletRequest request, HttpServletResponse response)
public View failureCallback(HttpServletRequest request, HttpServletResponse response)
public View profile(HttpServletRequest request, HttpServletResponse response)
```

Handles OAuth2 login callbacks and profile display.

## Migration from OpenID 2.0

If you previously used OpenID 2.0 authentication (removed in Spring Security 6.x):

1. **User Migration**: OAuth2 users will have different identifiers than OpenID 2.0 users
2. **Profile Update**: Users should re-link their accounts via OAuth2
3. **Role Preservation**: Manually migrate user roles to new OAuth2 accounts

## Future Enhancements

Potential improvements for future versions:

1. Support for additional OAuth2 providers (Microsoft, Facebook, etc.)
2. Account linking for users with both password and OAuth2 credentials
3. OAuth2 token refresh for long-lived sessions
4. Scope-based authorization for API access
5. Social features integration (profile pictures, social sharing)

## References

- [Spring Security OAuth2 Client Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [RFC 6749: OAuth 2.0 Authorization Framework](https://tools.ietf.org/html/rfc6749)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)
- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [GitHub OAuth2 Documentation](https://docs.github.com/en/developers/apps/building-oauth-apps)

## Support

For issues or questions:
- GitHub Issues: [Pebble Blog Issues](https://github.com/pebbleblog/pebble/issues)
- Documentation: Check Pebble Blog documentation
- Community: Pebble user mailing list
