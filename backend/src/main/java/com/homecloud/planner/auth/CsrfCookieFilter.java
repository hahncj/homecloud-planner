package com.homecloud.planner.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * With {@code CsrfTokenRequestAttributeHandler}, the CSRF token is resolved
 * lazily and only written to the XSRF-TOKEN cookie once something reads it.
 * A server-rendered app reads it implicitly by referencing {@code _csrf} in
 * a view; this app is a SPA with no views, so nothing ever would. Forcing
 * the read here on every request is what actually gets the cookie onto the
 * response so the frontend can pick it up — see ADR-0008.
 */
class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
