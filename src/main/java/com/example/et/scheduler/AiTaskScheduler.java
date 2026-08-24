package com.example.et.scheduler;

import com.example.et.model.ai.AiParsingTask;
import com.example.et.model.ai.Status;
import com.example.et.service.ai.AiService;
import com.example.et.service.ai.parsetask.AiParseTaskService;
import com.example.et.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiTaskScheduler {
  private final AiParseTaskService aiParseTaskService;
  private final AiService aiService;
  private final NotificationService notificationService;

  @Scheduled(fixedRate = 5000)
  public void processAiParsingTask() {
    List<AiParsingTask> pendingTasks = aiParseTaskService.getPendingTasksWithAppUser(Status.PENDING);

    for (AiParsingTask task : pendingTasks) {
      String userId = task.getAppUser() != null ? task.getAppUser().getId().toString() : null;
      String taskId = task.getId().toString();

      task.setStatus(Status.PROCESSING);
      aiParseTaskService.save(task);

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
        if (task.getStatus() == Status.COMPLETED) {
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
