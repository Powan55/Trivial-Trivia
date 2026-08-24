package com.example;

import com.example.api.v1.ApiProblemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

/**
 * One explicit filter chain, which is what makes the starter safe to add here.
 *
 * <p>{@code anyRequest().authenticated()} is the auto-configured default and it applies only
 * while no chain is defined. Defining this one replaces it, so the landing page, the menu, the
 * whole game loop, the question page and export stay open to anyone, and a guest is an anonymous
 * authentication rather than a special case.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository contexts,
            ApiProblemHandler apiProblemHandler) throws Exception {
        // CSRF and changeSessionId session fixation protection are on by default. Nothing here
        // turns either off, and both are the reason the forms carry a hidden token.
        http
                .authorizeHttpRequests(routes -> routes
                        .requestMatchers("/questions/import").hasRole("ADMIN")
                        .requestMatchers("/stats").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/rounds").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers("/", "/menu", "/play/**", "/questions",
                                "/loginMenu", "/loginServlet", "/createUser",
                                "/guest", "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        // The page and the processing URL keep the paths the views and the
                        // bookmarks already use.
                        .loginPage("/loginMenu")
                        .loginProcessingUrl("/loginServlet")
                        .defaultSuccessUrl("/menu")
                        .failureUrl("/loginMenu?error"))
                .logout(out -> out
                        // POST only, which is the framework default once CSRF is on. A GET that
                        // ends your session is something any page can trigger with an <img>.
                        .logoutSuccessUrl("/loginMenu"))
                .securityContext(context -> context.securityContextRepository(contexts))
                // formLogin's default is a redirect to /loginMenu on both a missing session and a
                // wrong role, which is right for a browser and wrong for an API client -- scoped
                // to /api/v1/** only, everything else keeps the redirect.
                //
                // The explicit catch-all entries below matter: once a second mapping exists here,
                // Spring Security stops using formLogin's own late-registered entry point as the
                // fallback for a path that matches neither mapping (it falls back to whichever
                // mapping was inserted first instead, which -- because this Customizer runs before
                // formLogin registers its own -- would otherwise be the API one). Registering the
                // pre-existing redirect behaviour explicitly for AnyRequestMatcher keeps every
                // non-API path exactly as it already was.
                .exceptionHandling(handling -> handling
                        .defaultAuthenticationEntryPointFor(apiProblemHandler,
                                new AntPathRequestMatcher("/api/v1/**"))
                        .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/loginMenu"),
                                AnyRequestMatcher.INSTANCE)
                        .defaultAccessDeniedHandlerFor(apiProblemHandler,
                                new AntPathRequestMatcher("/api/v1/**"))
                        .defaultAccessDeniedHandlerFor(new AccessDeniedHandlerImpl(),
                                AnyRequestMatcher.INSTANCE))
                // Any slot ahead of the DispatcherServlet works -- the nonce and the header are
                // both set before the chain continues. What matters is that it is in this chain
                // at all: its coverage is whatever spring.security.filter.dispatcher-types says,
                // so narrowing that property narrows CSP with it.
                .addFilterBefore(new ContentSecurityPolicyFilter(), HeaderWriterFilter.class);
        return http.build();
    }

    /**
     * Named as a bean so registration can sign a new account in through the same repository the
     * filter chain reads on the next request.
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository());
    }
}
