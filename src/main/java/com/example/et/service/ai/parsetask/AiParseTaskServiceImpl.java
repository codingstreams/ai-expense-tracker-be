package com.example.et.service.ai.parsetask;

import com.example.et.model.ai.AiParsingTask;
import com.example.et.model.ai.Status;
import com.example.et.repo.AiParsingTaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiParseTaskServiceImpl implements AiParseTaskService {
  private final AiParsingTaskRepo aiParsingTaskRepo;

  @Override
  public AiParsingTask save(AiParsingTask aiParsingTask) {
    return aiParsingTaskRepo.save(aiParsingTask);
  }

  @Override
  public List<AiParsingTask> getPendingTasksWithAppUser(Status status) {
    return aiParsingTaskRepo.findByStatusWithAppUser(status);
  }

  @Override
  public AiParsingTask getById(UUID id) {
    return aiParsingTaskRepo.findById(id).orElse(null);
  }

  @Override
  @Transactional
  public void unlinkTransaction(UUID transactionId) {
    aiParsingTaskRepo.unlinkTransaction(transactionId);
  }
}
