package com.sh.sh.pos.system.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        private final CustomAuthenticationEntryPoint authEntryPoint;

        private final JwtValidator jwtValidator;

        public SecurityConfig(CustomAuthenticationEntryPoint authEntryPoint, JwtValidator jwtValidator) {
                this.authEntryPoint = authEntryPoint;
                this.jwtValidator = jwtValidator;
        }

        // ── Public endpoints ──────────────────────────────────────────────────────

        private static final String[] PUBLIC_URLS = {
                        "/auth/**",
                        "/api/users/forgot-password",
                        "/api/users/reset-password",
                        "/api/auth/**",

                        // ── Swagger UI ────────────────────────────────────────────────────────
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-resources/**",
                        "/webjars/**",
        };

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http.sessionManagement(
                                management -> management
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers(PUBLIC_URLS).permitAll()

                                                // Store registration can list active types
                                                .requestMatchers(HttpMethod.GET, "/api/store-types/active").permitAll()

                                                // Only Super Admin can manage store types
                                                .requestMatchers(
                                                                "/api/store-types",
                                                                "/api/store-types/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/api/super-admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/**").authenticated().anyRequest().denyAll())
                                .addFilterBefore(jwtValidator,
                                                BasicAuthenticationFilter.class)
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .exceptionHandling(
                                                exceptionHandler -> exceptionHandler
                                                                .authenticationEntryPoint(authEntryPoint))
                                .build();

        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(8);
        }

        private CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();

                config.setAllowedOrigins(List.of(
                                "http://localhost:5173",
                                "http://localhost:3000"));

                config.setAllowedMethods(List.of(
                                "GET",
                                "POST",
                                "PUT",
                                "PATCH",
                                "DELETE",
                                "OPTIONS"));

                config.setAllowedHeaders(List.of(
                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "Origin"));

                config.setExposedHeaders(List.of(
                                "Authorization"));

                config.setAllowCredentials(true);
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration(
                                "/**",
                                config);

                return source;

        }

}