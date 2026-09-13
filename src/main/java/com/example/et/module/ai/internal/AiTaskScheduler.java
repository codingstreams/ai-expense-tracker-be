package com.example.et.module.ai.internal;

import com.example.et.module.ai.AiService;
import com.example.et.module.ai.parser.AiParseTaskService;
import com.example.et.module.ai.parser.AiParsingTask;
import com.example.et.module.notification.NotificationService;
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
    log.trace("AiTaskScheduler run initiated.");

    final var pendingTasks = aiParseTaskService.getPendingTasksWithAppUser(AiParsingTask.Status.PENDING);
    if (pendingTasks.isEmpty()) {
      log.trace("No pending AI parsing tasks found.");
      return;
    }

    log.info("Found {} pending AI parsing task(s) to process.", pendingTasks.size());

    for (AiParsingTask task : pendingTasks) {
      final var taskId = task.getId().toString();
      final var userId = task.getAppUser() != null ? task.getAppUser().getId().toString() : null;

      log.info("Starting processing for taskId: {}, userId: {}", taskId, userId);

      try {
        task.setStatus(AiParsingTask.Status.PROCESSING);
        aiParseTaskService.save(task);

        if (userId != null) {
          log.debug("Sending PROCESSING notification for taskId: {} to userId: {}", taskId, userId);
          notificationService.send(
              userId,
              taskId,
              NotificationService.NotificationEvent.AI_TASK_PROCESSING,
              Map.of("taskId", taskId, "status", "PROCESSING")
          );
        } else {
          log.warn("No userId associated with taskId: {}. Skipping step-start notification.", taskId);
        }

        // Execute parsing logic
        aiService.parse(task);

        // Handle result status post-parsing
        if (task.getStatus() == AiParsingTask.Status.COMPLETED) {
          final String transactionId = task.getTransaction() != null ? task.getTransaction().getId().toString() : "";
          log.info("Task completed successfully. taskId: {}, transactionId: {}", taskId, transactionId);

          if (userId != null) {
            notificationService.send(
                userId,
                taskId,
                NotificationService.NotificationEvent.AI_TASK_COMPLETED,
                Map.of(
                    "taskId", taskId,
                    "status", "COMPLETED",
                    "transactionId", transactionId
                )
            );
          }
        } else {
          final String errorMsg = task.getErrorMessage() != null ? task.getErrorMessage() : "Parsing failed";
          log.warn("Task finished with non-completed status. taskId: {}, status: {}, failure: {}",
              taskId, task.getStatus(), errorMsg);

          if (userId != null) {
            notificationService.send(
                userId,
                taskId,
                NotificationService.NotificationEvent.AI_TASK_FAILED,
                Map.of(
                    "taskId", taskId,
                    "status", "FAILED",
                    "error", errorMsg
                )
            );
          }
        }
      } catch (Exception e) {
        log.error("Unhandled exception processing taskId: {}", taskId, e);
      } finally {
        if (userId != null) {
          log.debug("Closing notification connection for taskId: {}, userId: {}", taskId, userId);
          notificationService.closeConnection(userId, taskId);
        }
      }
    }

    log.info("Finished processing batch of {} AI task(s).", pendingTasks.size());
  }
}