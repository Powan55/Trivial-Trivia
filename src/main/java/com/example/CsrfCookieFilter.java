package com.example;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Forces the deferred CSRF token to resolve on every request, not just the ones a JSP happens to
 * render.
 *
 * <p>{@code CsrfFilter} hands the request a lazily-resolved {@code CsrfToken} and only saves it
 * -- to the cookie, with {@code CookieCsrfTokenRepository} -- once something calls
 * {@code getToken()} on it. A JSP triggers that by evaluating {@code ${_csrf.token}}; a JSON
 * endpoint never touches the attribute, so without this filter the cookie a non-JSP client needs
 * would never get written.</p>
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        chain.doFilter(request, response);
    }
}
