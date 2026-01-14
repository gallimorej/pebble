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
package net.sourceforge.pebble.mock;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

/**
 * A mock AuthenticationManager implementation, based upon the ships with Acegi.
 *
 * @author    Simon Brown
 */
public class MockAuthenticationManager implements AuthenticationManager {
    //~ Instance fields ========================================================

    private boolean grantAccess = true;

  private List<GrantedAuthority> authorities = new ArrayList<>();

    //~ Constructors ===========================================================

  public MockAuthenticationManager(boolean grantAccess) {
      this.grantAccess = grantAccess;
  }

  public MockAuthenticationManager(boolean grantAccess, GrantedAuthority[] authorities) {
      this.grantAccess = grantAccess;
    for (GrantedAuthority authority : authorities) {
      this.authorities.add(authority);
    }
  }

    public MockAuthenticationManager() {
    }

    //~ Methods ================================================================

    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        if (grantAccess) {
          return new TestingAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), authorities);
        } else {
            throw new BadCredentialsException(
                "MockAuthenticationManager instructed to deny access");
        }
    }

}
