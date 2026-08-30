package com.example.et.security;

import com.example.et.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;

@Component("bearerAuthProvider")
@RequiredArgsConstructor
public class BearerAuthProvider implements AuthenticationProvider {
  private final SecretKey secretKey;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    final var claims = JwtUtils.parseToken((authentication instanceof BearerAuthToken authToken) ? (String) authToken.getCredentials() : "",
        secretKey);

    final var username = claims.get(Claims.SUBJECT, String.class);

    final List<? extends GrantedAuthority> roles = JwtUtils.extractAuthorities(claims);

    return BearerAuthToken.authenticated(username, roles);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.isAssignableFrom(BearerAuthToken.class);
  }
}