package com.aegis.apigateway.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Spring Security configuration for the API Gateway using Spring WebFlux.
 * This class configures both the OAuth2 Client for the login flow and
 * the OAuth2 Resource Server for validating JWT tokens.
 */
@Configuration
@EnableWebFluxSecurity
public class Configurations {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository) {
        // Configure security for the gateway.
        http
                // All requests must be authenticated.
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .anyExchange().authenticated()
                )
                // Enable OAuth2 login.
                .oauth2Login(Customizer.withDefaults())
                // Configure logout.
                .logout(logoutSpec -> logoutSpec
                        // Use the OidcClientInitiatedServerLogoutSuccessHandler for Keycloak logout.
                        .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
                )
                // Disable CSRF for API Gateway.
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    private OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        // This handler automatically constructs the Keycloak logout URL with id_token_hint.
        OidcClientInitiatedServerLogoutSuccessHandler handler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);

        // Configure the URI to redirect to after Keycloak has completed its logout.
        // We redirect back to the root of the gateway.
        handler.setPostLogoutRedirectUri("http://localhost:8082/");

        return handler;
    }
}
