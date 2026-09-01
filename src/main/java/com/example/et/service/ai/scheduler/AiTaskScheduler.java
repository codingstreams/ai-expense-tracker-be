package com.example.et.service.ai.scheduler;

import com.example.et.model.ai.AiParsingTask;
import com.example.et.service.ai.AiService;
import com.example.et.service.ai.parsetask.AiParseTaskService;
import com.example.et.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiTaskScheduler {
  private final AiParseTaskService aiParseTaskService;
  private final AiService aiService;
  private final NotificationService notificationService;

  @Scheduled(fixedRate = 5000)
  public void processAiParsingTask() {
    log.info("AiTaskScheduler start");

    final var pendingTasks = aiParseTaskService.getPendingTasksWithAppUser(AiParsingTask.Status.PENDING);

    for (AiParsingTask task : pendingTasks) {
      final var userId = task.getAppUser() != null ? task.getAppUser().getId().toString() : null;
      final var taskId = task.getId().toString();

      task.setStatus(AiParsingTask.Status.PROCESSING);
      aiParseTaskService.save(task);

      // Send Notification
      if (userId != null) {
        notificationService.send(
            userId,
            taskId,
            NotificationService.NotificationEvent.AI_TASK_PROCESSING,
            Map.of("taskId", taskId, "status", "PROCESSING")
        );
      }

      aiService.parse(task);

      if (userId != null) {
        if (task.getStatus() == AiParsingTask.Status.COMPLETED) {
          notificationService.send(
              userId,
              taskId,
              NotificationService.NotificationEvent.AI_TASK_COMPLETED,
              Map.of(
                  "taskId", taskId,
                  "status", "COMPLETED",
                  "transactionId", task.getTransaction() != null ? task.getTransaction().getId().toString() : ""
              )
          );
        } else {
          notificationService.send(
              userId,
              taskId,
              NotificationService.NotificationEvent.AI_TASK_FAILED,
              Map.of(
                  "taskId", taskId,
                  "status", "FAILED",
                  "error", task.getErrorMessage() != null ? task.getErrorMessage() : "Parsing failed"
              )
          );
        }
        notificationService.closeConnection(userId, taskId);
      }
    }
  }
}
