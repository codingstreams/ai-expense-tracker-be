package com.example.et.config;

import com.example.et.config.props.JwtProps;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class AuthConfig {
  @Bean
  AuthenticationManager authenticationManager(
      @Qualifier("usernamePasswordAuthProvider") AuthenticationProvider usernamePasswordAuthProvider, @Qualifier("bearerAuthProvider") AuthenticationProvider bearerAuthProvider) {
    final var authenticationProvider = List.of(usernamePasswordAuthProvider, bearerAuthProvider);
    return new ProviderManager(authenticationProvider);
  }

  @Bean("usernamePasswordAuthProvider")
  AuthenticationProvider usernamePasswordAuthProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    final var daoAuthProvider = new DaoAuthenticationProvider(userDetailsService);
    daoAuthProvider.setPasswordEncoder(passwordEncoder);
    return daoAuthProvider;
  }

  @Bean
  SecretKey secretKey(JwtProps jwtProps) {
    return Keys.hmacShaKeyFor(jwtProps.getSecretKey()
        .getBytes(StandardCharsets.UTF_8));
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
