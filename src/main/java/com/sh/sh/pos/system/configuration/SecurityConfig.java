package com.sh.sh.pos.system.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomAuthenticationEntryPoint authEntryPoint;

        // ── Public endpoints ──────────────────────────────────────────────────────

        private static final String[] PUBLIC_URLS = {
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
                                                .requestMatchers(PUBLIC_URLS).permitAll()
                                                .requestMatchers("/api/**").authenticated()
                                                .requestMatchers("/api/super-admin/**").hasRole("ADMIN")
                                                .anyRequest().permitAll())
                                .addFilterBefore(new JwtValidator(),
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
                return (HttpServletRequest request) -> {
                        CorsConfiguration cfg = new CorsConfiguration();
                        cfg.setAllowedOrigins(List.of(
                                        "http://localhost:5173", // Vite dev server
                                        "http://localhost:3000" // React dev server
                        ));
                        cfg.setAllowedMethods(Collections.singletonList("*"));
                        cfg.setAllowedHeaders(Collections.singletonList("*"));
                        cfg.setExposedHeaders(List.of("Authorization"));
                        cfg.setAllowCredentials(true);
                        cfg.setMaxAge(3600L);
                        return cfg;
                };
        }

}