package com.example.et.module.ai.parser;

import java.util.List;
import java.util.UUID;

public interface AiParseTaskService {
  AiParsingTask save(AiParsingTask aiParsingTask);

  List<AiParsingTask> getPendingTasksWithAppUser(AiParsingTask.Status status);

  AiParsingTask getById(UUID id);

  void unlinkTransaction(UUID transactionId);
}
