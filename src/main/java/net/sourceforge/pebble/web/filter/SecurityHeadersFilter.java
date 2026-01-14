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

package net.sourceforge.pebble.web.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter that adds modern security headers to all HTTP responses.
 *
 * Security Headers Implemented (OWASP recommendations):
 *
 * 1. X-Frame-Options: SAMEORIGIN
 *    - Prevents clickjacking attacks by disallowing the page to be framed by external sites
 *    - Only allows framing from same origin (blog can frame itself)
 *
 * 2. X-Content-Type-Options: nosniff
 *    - Prevents browsers from MIME-sniffing responses away from declared content-type
 *    - Reduces risk of drive-by downloads and MIME confusion attacks
 *
 * 3. X-XSS-Protection: 1; mode=block
 *    - Enables browser's built-in XSS filter (legacy but still useful for older browsers)
 *    - Modern browsers use CSP instead, but this provides defense-in-depth
 *
 * 4. Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'
 *    - Modern XSS protection mechanism
 *    - Restricts resource loading to same-origin by default
 *    - Allows inline scripts/styles (required for Pebble's JSP-based UI)
 *    - Production deployments should tighten CSP further and remove 'unsafe-inline' where possible
 *
 * 5. Referrer-Policy: strict-origin-when-cross-origin
 *    - Controls how much referrer information is included with requests
 *    - Sends origin when navigating to external sites (privacy-preserving)
 *    - Sends full URL for same-origin requests (useful for analytics)
 *
 * 6. Strict-Transport-Security: max-age=31536000; includeSubDomains
 *    - HSTS header forces HTTPS for 1 year (31536000 seconds)
 *    - Includes all subdomains in the policy
 *    - Only added when request is over HTTPS (per RFC 6797 requirement)
 *
 * @author Claude Code Security Enhancements
 */
public class SecurityHeadersFilter implements Filter {

    private static final Log log = LogFactory.getLog(SecurityHeadersFilter.class);

    /**
     * Initializes the filter.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Security Headers Filter initialized - adding OWASP-recommended security headers to all responses");
    }

    /**
     * Adds security headers to the HTTP response.
     *
     * @param request  the ServletRequest
     * @param response the ServletResponse
     * @param chain    the FilterChain
     * @throws IOException, ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // 1. X-Frame-Options: Prevent clickjacking
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");

            // 2. X-Content-Type-Options: Prevent MIME sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // 3. X-XSS-Protection: Enable browser XSS filter (legacy support)
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // 4. Content-Security-Policy: Modern XSS protection
            // Note: 'unsafe-inline' is required for Pebble's JSP-based UI
            // Production deployments should consider moving inline scripts to external files
            String csp = "default-src 'self'; " +
                         "script-src 'self' 'unsafe-inline'; " +
                         "style-src 'self' 'unsafe-inline'; " +
                         "img-src 'self' data: https:; " + // Allow images from HTTPS and data URIs
                         "font-src 'self'; " +
                         "connect-src 'self'; " +
                         "frame-ancestors 'self'; " +
                         "form-action 'self'";
            httpResponse.setHeader("Content-Security-Policy", csp);

            // 5. Referrer-Policy: Control referrer information leakage
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // 6. Strict-Transport-Security (HSTS): Force HTTPS
            // Only add HSTS header if request is over HTTPS (per RFC 6797)
            if (isSecure(httpRequest)) {
                // max-age=31536000 (1 year), includeSubDomains applies to all subdomains
                httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            }
        }

        // Continue filter chain
        chain.doFilter(request, response);
    }

    /**
     * Checks if the request is secure (HTTPS).
     * Handles both direct HTTPS and proxy scenarios (X-Forwarded-Proto header).
     *
     * @param request the HTTP servlet request
     * @return true if request is over HTTPS, false otherwise
     */
    private boolean isSecure(HttpServletRequest request) {
        // Check if request is directly over HTTPS
        if (request.isSecure()) {
            return true;
        }

        // Check X-Forwarded-Proto header (for proxy/load balancer scenarios)
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        return "https".equalsIgnoreCase(forwardedProto);
    }

    /**
     * Destroys the filter.
     */
    @Override
    public void destroy() {
        // No cleanup required
    }
}
