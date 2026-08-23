package com.example.et.config;

import com.example.et.config.props.JwtProps;
import com.example.et.security.BearerAuthProvider;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(JwtProps.class)
public class AuthConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(
      @Qualifier("usernamePasswordAuthProvider") AuthenticationProvider usernamePasswordAuthProvider,
      @Qualifier("bearerAuthProvider") BearerAuthProvider bearerAuthProvider) {
    final var authenticationProvider = List.of(usernamePasswordAuthProvider, bearerAuthProvider);
    return new ProviderManager(authenticationProvider);
  }

  @Bean("usernamePasswordAuthProvider")
  AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
    final var authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    return authenticationProvider;
  }

  @Bean
  SecretKey secretKey(JwtProps jwtProps) {
    return Keys.hmacShaKeyFor(jwtProps.getSecretKey()
        .getBytes(StandardCharsets.UTF_8));
  }
}
