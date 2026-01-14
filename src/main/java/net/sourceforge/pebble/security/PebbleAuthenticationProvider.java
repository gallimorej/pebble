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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Custom authentication provider that extends Spring Security's DaoAuthenticationProvider
 * to support automatic password upgrade from SHA-1 to BCrypt.
 *
 * This provider:
 * 1. Sets the username in ThreadLocal before authentication (for PebblePasswordEncoder)
 * 2. Delegates to parent DaoAuthenticationProvider for actual authentication
 * 3. Cleans up ThreadLocal after authentication completes
 *
 * The password upgrade logic is handled by PebblePasswordEncoder, which detects
 * legacy SHA-1 passwords and upgrades them to BCrypt on successful authentication.
 *
 * @author Claude Code Security Enhancements
 */
public class PebbleAuthenticationProvider extends DaoAuthenticationProvider {

    private static final Log log = LogFactory.getLog(PebbleAuthenticationProvider.class);

    /**
     * Authenticates the user, with support for automatic password upgrading.
     *
     * @param authentication the authentication request object
     * @return a fully authenticated object including credentials
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Extract username from authentication request
        String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();

        try {
            // Set username in ThreadLocal for PebblePasswordEncoder to use during password validation
            PebblePasswordEncoder.setCurrentUsername(username);

            log.debug("Authenticating user: " + username);

            // Delegate to parent DaoAuthenticationProvider for actual authentication
            // The parent will call UserDetailsService.loadUserByUsername() and PasswordEncoder.matches()
            // If PebblePasswordEncoder detects a legacy SHA-1 password, it will upgrade it to BCrypt
            Authentication result = super.authenticate(authentication);

            log.debug("Authentication successful for user: " + username);
            return result;

        } catch (BadCredentialsException e) {
            log.debug("Authentication failed for user: " + username + " - " + e.getMessage());
            throw e;
        } catch (AuthenticationException e) {
            log.warn("Authentication error for user: " + username, e);
            throw e;
        } finally {
            // Always clean up ThreadLocal to prevent memory leaks
            PebblePasswordEncoder.clearCurrentUsername();
        }
    }
}
