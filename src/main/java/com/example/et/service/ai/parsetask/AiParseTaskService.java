package com.example.et.service.ai.parsetask;

import com.example.et.model.ai.AiParsingTask;

import java.util.List;

public interface AiParseTaskService {
  AiParsingTask save(AiParsingTask task);

  List<AiParsingTask> getPendingTasksWithAppUser(AiParsingTask.Status status);
}
