package com.hcl.inventory.config;

import com.hcl.inventory.filter.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security filter chain for the RIMS REST API.
 *
 * <ul>
 *   <li>POST /auth/login — public (no token required)</li>
 *   <li>GET /swagger-ui/** and /v3/api-docs/** — public (dev convenience)</li>
 *   <li>OPTIONS /** — public (CORS preflight)</li>
 *   <li>/users/** — ADMIN only</li>
 *   <li>/orders/**, /order-items/** — ADMIN, STORE_MANAGER, STORE_STAFF, WAREHOUSE_STAFF</li>
 *   <li>All other endpoints — require a valid JWT Bearer token</li>
 * </ul>
 *
 * Sessions are stateless; CSRF is disabled because only JWT is used.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF — not needed for stateless JWT REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // CORS is handled by CorsConfig bean
                .cors(cors -> {})

                // Stateless sessions — no HttpSession
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // Preflight CORS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Login endpoint — open to everyone (both /auth/login and /api/auth/login)
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/api/auth/login").permitAll()

                        // Swagger / OpenAPI — open in dev
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Administrative endpoints require ADMIN role
                        .requestMatchers("/users", "/users/**", "/api/users", "/api/users/**").hasRole("ADMIN")

                        // Customer Orders & Order Items
                        .requestMatchers(
                                "/orders", "/orders/**", "/api/orders", "/api/orders/**",
                                "/order-items", "/order-items/**", "/api/order-items", "/api/order-items/**"
                        ).hasAnyRole("ADMIN", "STORE_MANAGER", "STORE_STAFF", "WAREHOUSE_STAFF")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Insert JWT filter before username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
