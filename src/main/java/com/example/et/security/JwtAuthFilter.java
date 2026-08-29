package com.example.et.security;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final AuthenticationManager authenticationManager;

  public static Optional<String> extractToken(String authorizationHeader) {
    if (StringUtils.isBlank(authorizationHeader)) {
      return Optional.empty();
    }
    return Optional.of(authorizationHeader.substring(7));
  }


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    final var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    final var token = extractToken(authHeader);

    if (token.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    final var unauthenticatedToken = BearerAuthToken.unauthenticated(token.get());

    try {
      final var authenticatedToken = authenticationManager.authenticate(unauthenticatedToken);

      SecurityContextHolder.getContext()
          .setAuthentication(authenticatedToken);

    } catch (AuthenticationException e) {
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
      return;
    }

    filterChain.doFilter(request, response);
  }
}
