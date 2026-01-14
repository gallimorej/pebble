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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Custom password encoder that supports automatic password upgrade from SHA-1 to BCrypt.
 *
 * This encoder implements a migration strategy where:
 * 1. New passwords are encoded with BCrypt (OWASP recommended)
 * 2. Existing SHA-1 passwords continue to work
 * 3. On successful SHA-1 authentication, the password is automatically upgraded to BCrypt
 *
 * Password Format Detection:
 * - BCrypt passwords start with "$2a$", "$2b$", or "$2y$"
 * - SHA-1 passwords are 40-character hexadecimal strings (legacy format)
 *
 * @author Claude Code Security Enhancements
 */
public class PebblePasswordEncoder implements PasswordEncoder {

    private static final Log log = LogFactory.getLog(PebblePasswordEncoder.class);

    private PasswordEncoder modernEncoder;  // BCrypt
    private PasswordEncoder legacyEncoder;  // SHA-1
    private SecurityRealm securityRealm;

    // Thread-local storage for username during authentication (used for password upgrading)
    private static final ThreadLocal<String> currentUsername = new ThreadLocal<>();

    /**
     * Encodes the raw password using BCrypt (modern encoder).
     *
     * @param rawPassword the raw password to encode
     * @return BCrypt-encoded password
     */
    @Override
    public String encode(CharSequence rawPassword) {
        return modernEncoder.encode(rawPassword);
    }

    /**
     * Verifies the raw password against the encoded password, supporting both BCrypt and SHA-1.
     * Automatically upgrades SHA-1 passwords to BCrypt on successful authentication.
     *
     * @param rawPassword     the raw password to verify
     * @param encodedPassword the encoded password from the database
     * @return true if passwords match, false otherwise
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            log.warn("Encoded password is null or empty");
            return false;
        }

        // Check if password is BCrypt format (starts with $2a$, $2b$, or $2y$)
        if (isBCryptFormat(encodedPassword)) {
            log.debug("Validating BCrypt password");
            return modernEncoder.matches(rawPassword, encodedPassword);
        }

        // Assume legacy SHA-1 format (40-character hex string)
        log.debug("Validating legacy SHA-1 password");
        boolean matches = legacyEncoder.matches(rawPassword, encodedPassword);

        if (matches) {
            log.info("Legacy SHA-1 password validated successfully - triggering upgrade to BCrypt");
            upgradePasswordToBCrypt(rawPassword);
        }

        return matches;
    }

    /**
     * Checks if a password is in BCrypt format.
     *
     * @param encodedPassword the encoded password to check
     * @return true if BCrypt format, false otherwise
     */
    private boolean isBCryptFormat(String encodedPassword) {
        return encodedPassword.startsWith("$2a$") ||
               encodedPassword.startsWith("$2b$") ||
               encodedPassword.startsWith("$2y$");
    }

    /**
     * Upgrades a legacy SHA-1 password to BCrypt.
     * This method is called after successful SHA-1 authentication.
     *
     * @param rawPassword the raw password (plaintext) that was successfully validated
     */
    private void upgradePasswordToBCrypt(CharSequence rawPassword) {
        String username = currentUsername.get();
        if (username == null) {
            log.warn("Cannot upgrade password: username not set in ThreadLocal (authentication context may be missing)");
            return;
        }

        try {
            // Get user details
            PebbleUserDetails user = securityRealm.getUser(username);
            if (user == null) {
                log.error("Cannot upgrade password: user not found - " + username);
                return;
            }

            // Encode password with BCrypt
            String bcryptHash = modernEncoder.encode(rawPassword);

            // Update user's password in security realm
            user.setPassword(bcryptHash);
            securityRealm.updateUser(user);

            log.info("Successfully upgraded password from SHA-1 to BCrypt for user: " + username);

        } catch (SecurityRealmException e) {
            log.error("Failed to upgrade password for user: " + username, e);
        } finally {
            // Clean up ThreadLocal to prevent memory leaks
            currentUsername.remove();
        }
    }

    /**
     * Sets the username in ThreadLocal for password upgrade tracking.
     * This should be called by the authentication provider before password validation.
     *
     * @param username the username being authenticated
     */
    public static void setCurrentUsername(String username) {
        currentUsername.set(username);
    }

    /**
     * Clears the username from ThreadLocal.
     * Should be called after authentication completes (success or failure).
     */
    public static void clearCurrentUsername() {
        currentUsername.remove();
    }

    // Getters and Setters

    public PasswordEncoder getModernEncoder() {
        return modernEncoder;
    }

    public void setModernEncoder(PasswordEncoder modernEncoder) {
        this.modernEncoder = modernEncoder;
    }

    public PasswordEncoder getLegacyEncoder() {
        return legacyEncoder;
    }

    public void setLegacyEncoder(PasswordEncoder legacyEncoder) {
        this.legacyEncoder = legacyEncoder;
    }

    public SecurityRealm getSecurityRealm() {
        return securityRealm;
    }

    public void setSecurityRealm(SecurityRealm securityRealm) {
        this.securityRealm = securityRealm;
    }
}
