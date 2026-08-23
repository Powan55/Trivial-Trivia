package com.example;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Sends a Content-Security-Policy with a fresh nonce per response.
 *
 * <p>The views escape everything through {@code <c:out>}. That is a convention, and a convention
 * holds until somebody forgets; this is the browser enforcing the same thing. The nonce exists
 * because {@code fragments/head.jspf} runs one inline script before first paint so a dark-theme
 * reader gets no white flash, and the alternative to a nonce is {@code 'unsafe-inline'}, which
 * would give away the entire header.</p>
 */
public class ContentSecurityPolicyFilter extends OncePerRequestFilter {

    /** Where the JSP reads it: {@code <script nonce="${cspNonce}">}. */
    public static final String NONCE_ATTRIBUTE = "cspNonce";

    private final StringKeyGenerator nonces = new Base64StringKeyGenerator();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String nonce = nonces.generateKey();
        request.setAttribute(NONCE_ATTRIBUTE, nonce);
        response.setHeader("Content-Security-Policy", policy(nonce));
        chain.doFilter(request, response);
    }

    static String policy(String nonce) {
        return "default-src 'self'; "
                + "script-src 'self' 'nonce-" + nonce + "'; "
                // Three views compute a percentage into a style attribute. A nonce cannot cover a
                // style attribute -- only a <style> element -- and the values change per request,
                // so a hash cannot either. style-src-elem keeps that concession off <style>
                // blocks in browsers that understand it; ones that do not fall back to style-src,
                // which is why the weaker directive is still there.
                + "style-src 'self' 'unsafe-inline'; "
                + "style-src-elem 'self'; "
                + "object-src 'none'; "
                + "base-uri 'self'; "
                + "form-action 'self'; "
                + "frame-ancestors 'none'";
    }
}
