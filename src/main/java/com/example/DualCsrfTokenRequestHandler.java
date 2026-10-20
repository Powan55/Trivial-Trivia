package com.example;

import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

/**
 * Accepts either the masked value a rendered form submits or the raw value a cookie-only client
 * mirrors back unmodified.
 *
 * <p>{@code CookieCsrfTokenRepository} always persists the raw token in the cookie.
 * {@code XorCsrfTokenRequestAttributeHandler} requires whatever comes back in the header or
 * parameter to be a freshly masked encoding of that raw token -- a client that only ever reads
 * {@code document.cookie} (or curl, or Postman) has no way to produce that encoding; it can only
 * mirror what it read. Every rendered page still gets the masked value from {@code handle()}, so
 * the BREACH protection on that path is unchanged. The fallback is a plain equality check on
 * whatever the request actually sent -- the same double-submit-cookie comparison Spring shipped
 * as the only option before the masked handler existed -- and it only ever runs after the masked
 * check has already failed, so it costs nothing on the path that already works.</p>
 */
public class DualCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final XorCsrfTokenRequestAttributeHandler masked = new XorCsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestAttributeHandler raw = new CsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        masked.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String unmasked = masked.resolveCsrfTokenValue(request, csrfToken);
        return (unmasked != null) ? unmasked : raw.resolveCsrfTokenValue(request, csrfToken);
    }
}
