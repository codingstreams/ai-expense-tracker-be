package com.example.et.config;

import com.example.et.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                          JwtAuthFilter jwtAuthFilter,
                                          AuthenticationEntryPoint authenticationEntryPoint) {

    httpSecurity
        .cors(CorsConfigurer::disable)
        .csrf(CsrfConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(http -> http
            .requestMatchers("/api/auth/**", "/error")
            .permitAll()
            .anyRequest()
            .authenticated())
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint));

    return httpSecurity.build();
  }
}
