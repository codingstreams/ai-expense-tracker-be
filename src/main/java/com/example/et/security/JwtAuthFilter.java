package com.example.et.security;

import com.example.et.service.auth.ExpireTokenService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
  private final AuthenticationManager authenticationManager;
  private final ExpireTokenService expireTokenService;

  public static Optional<String> extractToken(String authorizationHeader) {
    if (StringUtils.isBlank(authorizationHeader)) {
      return Optional.empty();
    }
    return Optional.of(authorizationHeader.substring(7));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {
    // Extract authorization header
    final var requestHeader = request.getHeader(HttpHeaders.AUTHORIZATION);// Bearer <token>
    final var token = extractToken(requestHeader);

    if (token.isEmpty()) {
      filterChain.doFilter(request, response);
      return;
    }

    if (expireTokenService.isExpireToken(token.get())) {
      filterChain.doFilter(request, response);
      return;
    }

    final var unauthenticatedToken = BearerAuthToken.unauthenticated(token.get());
    try {
      final var authenticatedToken = authenticationManager.authenticate(unauthenticatedToken);

      SecurityContextHolder.getContext()
          .setAuthentication(authenticatedToken);

      // Set the tenant context for Hibernate filter scoping
//      final var principal = (String) authenticatedToken.getPrincipal();
//      TenantContext.setTenantId(principal);

    } catch (AuthenticationException e) {
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
      return;
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
//      TenantContext.clear();
    }
  }
}
