# Pebble TODO List

## Future Enhancements

### Authentication
- [ ] Add modern OAuth 2.0 / OpenID Connect support using Spring Security 6 OAuth2 Client
  - Google OAuth 2.0
  - GitHub OAuth 2.0
  - Other OAuth providers
  - Note: Old OpenID 2.0 was removed in Spring Security 6 migration (Phase 3B-R)
  - Use `spring-boot-starter-oauth2-client` or `spring-security-oauth2-client`
  - References:
    - https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html
    - https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html

## Completed
- [x] Phase 1: Java 6 to Java 8 migration with modern tooling
- [x] Phase 2: Spring 3.x to Spring 5.x + security enhancements
- [x] Phase 3A: Java 8 to Java 17 LTS compilation
- [x] Phase 3B-R: Spring 6 + Jakarta EE + library replacements
  - Spring 6.0.23
  - Spring Security 6.2.1
  - Jakarta Servlet 5.0
  - Tomcat 10.1.19
  - All tests passing (775/775)
