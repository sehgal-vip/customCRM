package com.turno.crm.config;

import com.turno.crm.security.ApiKeyAuthFilter;
import com.turno.crm.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthFilter apiKeyAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ApiKeyAuthFilter apiKeyAuthFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.apiKeyAuthFilter = apiKeyAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(fo -> fo.deny())
                        .contentTypeOptions(Customizer.withDefaults())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/users", "/api/v1/auth/dev-login").permitAll()
                        .requestMatchers("/api/v1/webhook/leads").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @org.springframework.beans.factory.annotation.Value("${cors.origins:http://localhost:5173}")
    private String corsOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(corsOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-API-Key"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
