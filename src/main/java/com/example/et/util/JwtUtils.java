package com.example.et.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.www.NonceExpiredException;

import javax.crypto.SecretKey;
import java.time.Instant;

import java.util.*;

public interface JwtUtils {
  static String generateAccessToken(String username,
                                    Collection<? extends GrantedAuthority> roles,
                                    SecretKey secretKey,
                                    long expirationTimeAccessToken) {
    return generateToken(username, roles, secretKey, expirationTimeAccessToken, false
    );
  }

  private static String generateToken(String username,
                                      Collection<? extends GrantedAuthority> roles,
                                      SecretKey secretKey,
                                      long expirationTime,
                                      boolean isRefreshToken) {
    final var jwtBuilder = Jwts.builder();

    final var now = Instant.now();
    final var expiration = now.plusSeconds(expirationTime);

    final var claims = Jwts.claims()
        .subject(username)
        .id(UUID.randomUUID()
            .toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .add("roles",
            isRefreshToken ? Collections.emptyList() : roles.stream()
                .map(GrantedAuthority::getAuthority)
                .toList())
        .build();

    return jwtBuilder
        .claims(claims)
        .signWith(secretKey)
        .compact();
  }

  static String generateRefreshToken(String username,
                                     SecretKey secretKey,
                                     long expirationTimeRefreshToken) {
    return generateToken(username, null, secretKey, expirationTimeRefreshToken, true
    );
  }

  static Claims parseToken(String s,
                           SecretKey secretKey) {
    final var jwtParser = Jwts.parser()
        .verifyWith(secretKey)
        .build();

    try {
      return jwtParser.parseSignedClaims(s)
          .getPayload();
    } catch (ExpiredJwtException e) {
      throw new NonceExpiredException(e.getMessage());
    } catch (JwtException | IllegalArgumentException e) {
      throw new RuntimeException(e);
    }
  }

  static List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
    final Object rolesObj = claims.get("roles");

    final List<?> roles = (rolesObj instanceof List<?> list) ? list : Collections.emptyList();

    if (roles.isEmpty()) {
      return Collections.emptyList();
    }

    return roles.stream()
        .map(role -> new SimpleGrantedAuthority((String) role))
        .toList();
  }

  static Claims getClaimsFromToken(String s,
                                   SecretKey secretKey) {
    final var jwtParser = Jwts.parser()
        .verifyWith(secretKey)
        .build();

    try {
      return jwtParser.parseSignedClaims(s)
          .getPayload();

    } catch (ExpiredJwtException e) {
      return e.getClaims();

    } catch (JwtException | IllegalArgumentException e) {
      throw new RuntimeException("Invalid token provider", e);
    }
  }
}
