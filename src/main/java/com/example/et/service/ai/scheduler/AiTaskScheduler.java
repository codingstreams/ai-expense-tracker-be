package com.example.et.service.ai.scheduler;

import com.example.et.model.ai.AiParsingTask;
import com.example.et.service.ai.AiService;
import com.example.et.service.ai.parsetask.AiParseTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiTaskScheduler {
  private final AiParseTaskService aiParseTaskService;
  private final AiService aiService;

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


      aiService.parse(task);
    }
  }
}
