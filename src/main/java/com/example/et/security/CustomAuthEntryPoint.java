package com.example.et.security;

import com.example.et.controller.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper;
  @Override
  public void commence(@NonNull HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException {
    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    final var errorResponse = new ApiResponse(
        HttpServletResponse.SC_UNAUTHORIZED,
        authException.getMessage(),
        Instant.now().toString()
    );

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}