package com.example.et.service.notification;

import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {
  SseEmitter openConnection(String userId, String sessionId);

  void send(String userId, String sessionId, NotificationEvent event, Object data);

  void closeConnection(String userId, String sessionId);

  @Getter
  enum NotificationEvent {
    // AI
    AI_TASK_CREATED("AI_TASK_CREATED"),
    AI_TASK_PROCESSING("AI_TASK_PROCESSING"),
    AI_TASK_COMPLETED("AI_TASK_COMPLETED"),
    AI_TASK_FAILED("AI_TASK_FAILED"),
    AI_TASK_ERROR("AI_TASK_ERROR"),
    AI_TASK_QUEUED("AI_TASK_QUEUED"),
    CONNECTED("CONNECTED");

    private final String event;

    NotificationEvent(String event) {
      this.event = event;
    }
  }
}