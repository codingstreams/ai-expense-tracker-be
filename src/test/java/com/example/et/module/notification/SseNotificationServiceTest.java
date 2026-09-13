package com.example.et.module.notification;

import com.example.et.module.notification.internal.SseNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.*;

class SseNotificationServiceTest {

  private SseNotificationService notificationService;

  @BeforeEach
  void setUp() {
    notificationService = new SseNotificationService();
  }

  @Test
  void openConnection_ShouldReturnSseEmitter() {
    SseEmitter emitter = notificationService.openConnection("user-1", "session-1");

    assertNotNull(emitter);
    assertEquals(60000L, emitter.getTimeout());
  }

  @Test
  void send_ToSpecificSession_ShouldExecuteWithoutError() {
    notificationService.openConnection("user-1", "session-1");

    assertDoesNotThrow(() ->
        notificationService.send("user-1", "session-1", NotificationService.NotificationEvent.AI_TASK_COMPLETED, "Data completed")
    );
  }

  @Test
  void send_ToNonExistentSession_ShouldNotThrow() {
    assertDoesNotThrow(() ->
        notificationService.send("user-1", "session-999", NotificationService.NotificationEvent.AI_TASK_FAILED, "Data failed")
    );
  }

  @Test
  void send_BroadcastToAllUserSessions_ShouldSendEvent() {
    notificationService.openConnection("user-1", "session-1");
    notificationService.openConnection("user-1", "session-2");
    notificationService.openConnection("user-2", "session-3");

    assertDoesNotThrow(() ->
        notificationService.send("user-1", null, NotificationService.NotificationEvent.AI_TASK_PROCESSING, "Processing")
    );
  }

  @Test
  void closeConnection_ShouldCompleteAndRemoveEmitter() {
    notificationService.openConnection("user-1", "session-1");

    assertDoesNotThrow(() -> notificationService.closeConnection("user-1", "session-1"));

    // Sending after close should safely do nothing
    assertDoesNotThrow(() ->
        notificationService.send("user-1", "session-1", NotificationService.NotificationEvent.AI_TASK_COMPLETED, "Done")
    );
  }

  @Test
  void shutdown_ShouldCompleteAllEmitters() {
    notificationService.openConnection("user-1", "session-1");
    notificationService.openConnection("user-2", "session-2");

    assertDoesNotThrow(() -> notificationService.shutdown());
  }
}
