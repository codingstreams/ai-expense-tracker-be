package com.example.et.service.notification;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class SseNotificationService implements NotificationService {
  private final ConcurrentMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();
  private static final long TIMEOUT = 60L * 1000;

  @PreDestroy
  public void shutdown() {
    emitters.forEach((key, emitter) -> {
      try {
        emitter.complete();
      } catch (Exception e) {
        log.warn("Failed to complete emitter for session: {}", key);
      }
    });
    emitters.clear();
  }

  @Override
  public SseEmitter openConnection(String userId, String sessionId) {
    String key = userId + ":" + sessionId;
    SseEmitter emitter = new SseEmitter(TIMEOUT);

    emitters.put(key, emitter);
    emitter.onCompletion(() -> emitters.remove(key));
    emitter.onTimeout(() -> {
      emitters.remove(key);
      emitter.complete();
    });
    emitter.onError((e) -> emitters.remove(key));

    try {
      emitter.send(SseEmitter.event().name(NotificationEvent.CONNECTED.getEvent()).data("Connected"));
    } catch (IOException e) {
      emitters.remove(key);
    }

    return emitter;
  }

  @Override
  public void send(String userId, String sessionId, NotificationEvent event, Object data) {
    if (sessionId != null) {
      SseEmitter emitter = emitters.get(userId + ":" + sessionId);
      if (emitter != null) {
        try {
          emitter.send(SseEmitter.event().name(event.getEvent()).data(data));
        } catch (IOException e) {
          emitters.remove(userId + ":" + sessionId);
        }
      }
    } else {
      emitters.forEach((key, emitter) -> {
        if (key.startsWith(userId + ":")) {
          try {
            emitter.send(SseEmitter.event().name(event.getEvent()).data(data));
          } catch (IOException e) {
            emitters.remove(key);
          }
        }
      });
    }
  }

  @Override
  public void closeConnection(String userId, String sessionId) {
    String key = userId + ":" + sessionId;
    SseEmitter emitter = emitters.remove(key);
    if (emitter != null) {
      emitter.complete();
    }
  }
}
