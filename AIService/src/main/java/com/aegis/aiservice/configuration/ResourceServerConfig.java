package com.aegis.aiservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring Security configuration for a reactive resource server.
 * This class configures the application to accept and validate JWT tokens
 * issued by a Keycloak authorization server.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class ResourceServerConfig {

    /**
     * Configures the main security filter chain for the application.
     * It ensures all endpoints require authentication and sets up the
     * OAuth2 resource server to process JWT tokens.
     *
     * @param http The ServerHttpSecurity object to configure.
     * @return A configured SecurityWebFilterChain.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges ->
                        exchanges
                                // Enforce authentication for all requests.
                                .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt -> jwt
                                // Uses our custom converter to extract roles from the JWT.
                                .jwtAuthenticationConverter(jwtAuthenticationConverterAdapter())
                        )
                )
                .csrf(csrf -> csrf.disable()); // CSRF is typically disabled for stateless APIs.

        return http.build();
    }

    /**
     * This is the core non-reactive converter.
     * It extracts the roles from the JWT and creates an AbstractAuthenticationToken.
     *
     * @return A Converter that transforms a Jwt into an AbstractAuthenticationToken.
     */
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverterInternal() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = extractKeycloakRoles(jwt);
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }

    /**
     * This bean wraps the non-reactive converter into a ReactiveJwtAuthenticationConverterAdapter.
     * This is required for compatibility with Spring WebFlux's reactive security context.
     *
     * @return A ReactiveJwtAuthenticationConverterAdapter.
     */
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverterAdapter() {
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverterInternal());
    }

    /**
     * Helper method to safely extract roles from Keycloak JWT claims.
     * It handles cases where claims or nested maps might be missing.
     *
     * @param jwt The JWT to inspect.
     * @return A collection of GrantedAuthority objects representing the user's roles.
     */
    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        // Safely extract realm-level roles.
        // `Optional.ofNullable` is used to prevent NullPointerExceptions.
        List<String> realmRoles = Optional.ofNullable(jwt.getClaimAsMap("realm_access"))
                .map(realmAccess -> (List<String>) realmAccess.get("roles"))
                .orElse(Collections.emptyList());

        // Safely extract client-specific roles.
        // IMPORTANT: Replace "aiservice-client" with your actual Keycloak client ID.
        String llmServiceClientId = "aiservice-client";
        List<String> clientRoles = Optional.ofNullable(jwt.getClaimAsMap("resource_access"))
                .map(resourceAccess -> (Map<String, Object>) resourceAccess.get(llmServiceClientId))
                .map(llmClientAccess -> (List<String>) llmClientAccess.get("roles"))
                .orElse(Collections.emptyList());

        return Stream.concat(realmRoles.stream(), clientRoles.stream())
                // Role names are prepended with "ROLE_" and converted to uppercase,
                // which is standard practice in Spring Security.
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }
}
