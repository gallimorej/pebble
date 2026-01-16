# OAuth2 Quick Start Guide

## What Was Implemented

OAuth 2.0 / OpenID Connect authentication support has been added to Pebble Blog, allowing users to login with:
- **Google** (OpenID Connect)
- **GitHub** (OAuth 2.0)

## Files Added

### Core Implementation
1. **OAuth2SecurityConfig.java** - OAuth2 client configuration
2. **OAuth2UserService.java** - Custom user service for OAuth2 integration
3. **OAuth2LoginController.java** - OAuth2 login callbacks handler
4. **application-oauth2.yml** - OAuth2 configuration template

### Updated Files
1. **pom.xml** - Added OAuth2 dependencies
2. **applicationContext-security.xml** - Added OAuth2 Spring Security configuration
3. **SecurityRealm.java** - Added convenience method for OAuth2 user creation

### Documentation
1. **OAuth2_Authentication_Guide.md** - Complete setup and usage guide
2. **OAuth2_Implementation_Summary.md** - Detailed implementation notes

## Quick Setup (5 Steps)

### Step 1: Get OAuth2 Credentials

**Google:**
1. Go to https://console.cloud.google.com/
2. Create OAuth client ID
3. Add redirect URI: `http://localhost:8080/login/oauth2/code/google`
4. Copy Client ID and Secret

**GitHub:**
1. Go to https://github.com/settings/developers
2. Create new OAuth App
3. Add callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Copy Client ID and Secret

### Step 2: Configure Application

Copy and edit the configuration file:

```bash
cp src/main/resources/application-oauth2.yml src/main/resources/application.yml
```

Edit `application.yml` and replace:
- `YOUR_GOOGLE_CLIENT_ID` with your Google Client ID
- `YOUR_GOOGLE_CLIENT_SECRET` with your Google Client Secret
- `YOUR_GITHUB_CLIENT_ID` with your GitHub Client ID
- `YOUR_GITHUB_CLIENT_SECRET` with your GitHub Client Secret

### Step 3: Enable OAuth2 in Spring Security

Edit `src/main/webapp/WEB-INF/applicationContext-security.xml`:

1. Find the section marked `<!-- OAuth2 Client Registration Repository -->`
2. Uncomment all OAuth2 bean definitions (search for `<!--` and remove comment markers)
3. Ensure these beans are uncommented:
   - `clientRegistrationRepository`
   - `oauth2LoginFilter`
   - `oauth2AuthenticationManager`
   - `oauth2AccessTokenResponseClient`
   - `oauth2AuthorizedClientService`

### Step 4: Build and Deploy

```bash
mvn clean package
# Deploy the generated WAR to Tomcat
```

### Step 5: Test

1. Navigate to login page: `http://localhost:8080/loginPage.action`
2. You should see "Login with Google" and "Login with GitHub" buttons
3. Click a button and complete OAuth2 flow
4. Verify you're logged in and user is created

## Configuration Properties

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

pebble:
  security:
    oauth2:
      auto-register-users: true  # Auto-create accounts for new OAuth2 users
      default-role: BLOG_READER   # Default role for new users
```

## Key Features

- ✅ **Auto-registration**: New OAuth2 users automatically get accounts
- ✅ **Profile sync**: Name and email automatically synced from provider
- ✅ **Secure**: Built on Spring Security 6.2.2
- ✅ **Backward compatible**: Existing password authentication still works
- ✅ **Extensible**: Easy to add more OAuth2 providers

## User Management

### New OAuth2 Users
- Automatically created with `BLOG_READER` role
- Username format: `google_123456` or `github_456789`
- Profile synced from OAuth2 provider

### Granting Additional Roles
Administrators can grant additional roles via Pebble admin interface:
1. Login as admin
2. Navigate to user management
3. Find OAuth2 user (search by `google_` or `github_` prefix)
4. Grant roles: BLOG_CONTRIBUTOR, BLOG_PUBLISHER, BLOG_OWNER, or BLOG_ADMIN

## Production Deployment

### Security Checklist
- [ ] Use environment variables for secrets (don't commit to git)
- [ ] Enable HTTPS
- [ ] Update redirect URIs to production domain
- [ ] Set `forceHttps: true` in authentication entry point
- [ ] Configure secure session cookies
- [ ] Regularly rotate OAuth2 client secrets

### Environment Variables (Recommended)
```bash
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret
export GITHUB_CLIENT_ID=your_github_client_id
export GITHUB_CLIENT_SECRET=your_github_client_secret
```

## Troubleshooting

### OAuth2 Login Button Not Appearing
- Verify `clientRegistrationRepository` bean is uncommented in applicationContext-security.xml
- Check that application.yml has valid OAuth2 credentials
- Look for errors in application logs

### Redirect URI Mismatch Error
- Ensure redirect URI in OAuth2 provider settings exactly matches: `http://localhost:8080/login/oauth2/code/{provider}`
- For production, use: `https://yourdomain.com/login/oauth2/code/{provider}`

### Invalid Client Error
- Verify Client ID and Client Secret are correct
- Check that credentials haven't expired
- Ensure no extra whitespace in configuration

### User Not Created
- Check that `auto-register-users: true` in configuration
- Verify SecurityRealm supports user creation
- Check application logs for SecurityRealmException

## Documentation

For complete documentation, see:
- **OAuth2_Authentication_Guide.md** - Complete setup guide with all details
- **OAuth2_Implementation_Summary.md** - Technical implementation details

## Support

- GitHub Issues: https://github.com/pebbleblog/pebble/issues
- Documentation: `/docs/OAuth2_Authentication_Guide.md`
- Spring Security OAuth2: https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html
