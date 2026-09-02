package com.example.et.service.ai.parsetask;

import com.example.et.model.ai.AiParsingTask;

import java.util.List;
import java.util.UUID;

public interface AiParseTaskService {
  AiParsingTask save(AiParsingTask aiParsingTask);

  List<AiParsingTask> getPendingTasksWithAppUser(AiParsingTask.Status status);

  AiParsingTask getById(UUID id);

  void unlinkTransaction(UUID transactionId);
}
