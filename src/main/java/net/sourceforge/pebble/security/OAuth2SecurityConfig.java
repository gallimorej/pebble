/*
 * Copyright (c) 2003-2011, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.pebble.security;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth 2.0 / OpenID Connect configuration for Pebble Blog.
 * Provides client registrations for Google and GitHub OAuth providers.
 *
 * Configuration properties should be set in application.yml or environment variables:
 * - spring.security.oauth2.client.registration.google.client-id
 * - spring.security.oauth2.client.registration.google.client-secret
 * - spring.security.oauth2.client.registration.github.client-id
 * - spring.security.oauth2.client.registration.github.client-secret
 *
 * @author Pebble Development Team
 * @since 2.7.0
 */
public class OAuth2SecurityConfig {

    /**
     * Creates a ClientRegistrationRepository with configured OAuth2 providers.
     * Only providers with valid client credentials will be registered.
     *
     * @param googleClientId Google OAuth client ID (optional)
     * @param googleClientSecret Google OAuth client secret (optional)
     * @param githubClientId GitHub OAuth client ID (optional)
     * @param githubClientSecret GitHub OAuth client secret (optional)
     * @return ClientRegistrationRepository with configured providers
     */
    public static ClientRegistrationRepository clientRegistrationRepository(
            String googleClientId, String googleClientSecret,
            String githubClientId, String githubClientSecret) {

        List<ClientRegistration> registrations = new ArrayList<>();

        // Add Google provider if configured
        if (isValidCredentials(googleClientId, googleClientSecret)) {
            registrations.add(googleClientRegistration(googleClientId, googleClientSecret));
        }

        // Add GitHub provider if configured
        if (isValidCredentials(githubClientId, githubClientSecret)) {
            registrations.add(githubClientRegistration(githubClientId, githubClientSecret));
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }

    /**
     * Creates a Google OAuth 2.0 / OpenID Connect client registration.
     *
     * @param clientId Google OAuth client ID
     * @param clientSecret Google OAuth client secret
     * @return ClientRegistration for Google
     */
    private static ClientRegistration googleClientRegistration(String clientId, String clientSecret) {
        return ClientRegistration.withRegistrationId("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
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

    /**
     * Creates a GitHub OAuth 2.0 client registration.
     * Note: GitHub uses OAuth 2.0 but not OpenID Connect.
     *
     * @param clientId GitHub OAuth client ID
     * @param clientSecret GitHub OAuth client secret
     * @return ClientRegistration for GitHub
     */
    private static ClientRegistration githubClientRegistration(String clientId, String clientSecret) {
        return ClientRegistration.withRegistrationId("github")
                .clientId(clientId)
                .clientSecret(clientSecret)
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

    /**
     * Validates OAuth credentials are present and not placeholder values.
     *
     * @param clientId Client ID to validate
     * @param clientSecret Client secret to validate
     * @return true if credentials are valid, false otherwise
     */
    private static boolean isValidCredentials(String clientId, String clientSecret) {
        return clientId != null && !clientId.trim().isEmpty()
                && !clientId.equals("YOUR_GOOGLE_CLIENT_ID")
                && !clientId.equals("YOUR_GITHUB_CLIENT_ID")
                && clientSecret != null && !clientSecret.trim().isEmpty()
                && !clientSecret.equals("YOUR_GOOGLE_CLIENT_SECRET")
                && !clientSecret.equals("YOUR_GITHUB_CLIENT_SECRET");
    }
}
