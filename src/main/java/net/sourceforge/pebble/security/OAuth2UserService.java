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

import net.sourceforge.pebble.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

/**
 * Custom OAuth2UserService that integrates OAuth2 authentication with Pebble's
 * existing security infrastructure. This service processes OAuth2 user information
 * and creates or updates corresponding Pebble user accounts.
 *
 * Features:
 * - Auto-registration of new OAuth2 users with BLOG_READER role
 * - Integration with existing SecurityRealm for user management
 * - Extraction of user profile information from OAuth2 providers
 * - Support for Google (OpenID Connect) and GitHub OAuth2
 *
 * @author Pebble Development Team
 * @since 2.7.0
 */
public class OAuth2UserService extends DefaultOAuth2UserService {

    private static final Log log = LogFactory.getLog(OAuth2UserService.class);

    private SecurityRealm securityRealm;
    private boolean autoRegisterUsers = true;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Load user from OAuth2 provider
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception e) {
            log.error("Error processing OAuth2 user", e);
            throw new OAuth2AuthenticationException(
                new OAuth2Error("processing_error", "Error processing OAuth2 user: " + e.getMessage(), null)
            );
        }
    }

    /**
     * Process OAuth2 user information and integrate with Pebble security.
     *
     * @param userRequest OAuth2 user request
     * @param oauth2User OAuth2 user from provider
     * @return OAuth2User with Pebble authorities
     * @throws SecurityRealmException if user processing fails
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User)
            throws SecurityRealmException {

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String providerId = extractProviderId(oauth2User, registrationId);

        // Generate username from OAuth provider and user ID
        String username = generateUsername(registrationId, providerId);

        log.info("Processing OAuth2 user: " + username + " from provider: " + registrationId);

        // Check if user exists in Pebble's security realm
        PebbleUserDetails pebbleUser = securityRealm.getUser(username);

        if (pebbleUser == null && autoRegisterUsers) {
            // Auto-register new OAuth2 user
            pebbleUser = createNewOAuth2User(oauth2User, registrationId, username);
            log.info("Auto-registered new OAuth2 user: " + username);
        } else if (pebbleUser == null) {
            throw new SecurityRealmException("User not found and auto-registration is disabled: " + username);
        } else {
            // Update existing user's information from OAuth2 profile
            updateUserFromOAuth2(pebbleUser, oauth2User, registrationId);
            log.info("Updated existing OAuth2 user: " + username);
        }

        // Return OAuth2User with Pebble authorities
        return new DefaultOAuth2User(
            pebbleUser.getAuthorities(),
            oauth2User.getAttributes(),
            getUserNameAttributeName(registrationId)
        );
    }

    /**
     * Create a new Pebble user from OAuth2 profile information.
     *
     * @param oauth2User OAuth2 user from provider
     * @param registrationId OAuth2 provider registration ID
     * @param username Generated username
     * @return PebbleUserDetails for the new user
     * @throws SecurityRealmException if user creation fails
     */
    private PebbleUserDetails createNewOAuth2User(OAuth2User oauth2User, String registrationId, String username)
            throws SecurityRealmException {

        Map<String, Object> attributes = oauth2User.getAttributes();

        String name = extractName(attributes, registrationId);
        String email = extractEmail(attributes, registrationId);
        String website = extractWebsite(attributes, registrationId);

        // Create user with BLOG_READER role by default
        // Admin must manually grant higher privileges if needed
        String[] roles = new String[] { Constants.BLOG_READER_ROLE };
        Map<String, String> preferences = new HashMap<>();

        PebbleUserDetails newUser = new PebbleUserDetails(
            username,
            null, // No password for OAuth users
            name,
            email,
            website,
            "", // Empty profile initially
            roles,
            preferences,
            true // Details are updateable
        );

        // Create user in security realm
        securityRealm.createUser(username, "", name, email, website, "", roles);

        return newUser;
    }

    /**
     * Update existing Pebble user with OAuth2 profile information.
     *
     * @param pebbleUser Existing Pebble user
     * @param oauth2User OAuth2 user from provider
     * @param registrationId OAuth2 provider registration ID
     */
    private void updateUserFromOAuth2(PebbleUserDetails pebbleUser, OAuth2User oauth2User, String registrationId) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        // Only update if user details are updateable
        if (pebbleUser.isDetailsUpdateable()) {
            String name = extractName(attributes, registrationId);
            String email = extractEmail(attributes, registrationId);

            if (name != null && !name.isEmpty()) {
                pebbleUser.setName(name);
            }
            if (email != null && !email.isEmpty()) {
                pebbleUser.setEmailAddress(email);
            }
        }
    }

    /**
     * Extract provider-specific user ID.
     */
    private String extractProviderId(OAuth2User oauth2User, String registrationId) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        return switch (registrationId.toLowerCase()) {
            case "google" -> attributes.get("sub").toString();
            case "github" -> attributes.get("id").toString();
            default -> oauth2User.getName();
        };
    }

    /**
     * Generate a Pebble username from OAuth provider and user ID.
     */
    private String generateUsername(String registrationId, String providerId) {
        return registrationId.toLowerCase() + "_" + providerId;
    }

    /**
     * Extract user's display name from OAuth2 attributes.
     */
    private String extractName(Map<String, Object> attributes, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) attributes.get("name");
            case "github" -> {
                String name = (String) attributes.get("name");
                yield (name != null) ? name : (String) attributes.get("login");
            }
            default -> (String) attributes.get("name");
        };
    }

    /**
     * Extract user's email from OAuth2 attributes.
     */
    private String extractEmail(Map<String, Object> attributes, String registrationId) {
        return (String) attributes.get("email");
    }

    /**
     * Extract user's website from OAuth2 attributes.
     */
    private String extractWebsite(Map<String, Object> attributes, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "github" -> (String) attributes.get("blog"); // GitHub uses "blog" field for personal website
            default -> null;
        };
    }

    /**
     * Get the username attribute name for the OAuth2 provider.
     */
    private String getUserNameAttributeName(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> "sub";
            case "github" -> "id";
            default -> "id";
        };
    }

    // Getters and Setters

    public SecurityRealm getSecurityRealm() {
        return securityRealm;
    }

    public void setSecurityRealm(SecurityRealm securityRealm) {
        this.securityRealm = securityRealm;
    }

    public boolean isAutoRegisterUsers() {
        return autoRegisterUsers;
    }

    public void setAutoRegisterUsers(boolean autoRegisterUsers) {
        this.autoRegisterUsers = autoRegisterUsers;
    }
}
