package com.example.et.controller;

import com.example.et.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @GetMapping("/subscribe/{sessionId}")
  public ResponseEntity<SseEmitter> connect(
      @PathVariable("sessionId") String sessionId,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(notificationService.openConnection(userId, sessionId));
  }
}
