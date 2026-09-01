package com.example.et.service.ai.parsetask;

import com.example.et.model.ai.AiParsingTask;
import com.example.et.repo.AiParseTaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiParseTaskServiceImpl implements AiParseTaskService {
  private final AiParseTaskRepo aiParseTaskRepo;

  @Override
  public AiParsingTask save(AiParsingTask task) {
    return aiParseTaskRepo.save(task);
  }

  @Override
  public List<AiParsingTask> getPendingTasksWithAppUser(AiParsingTask.Status status) {
    return aiParseTaskRepo.findByStatusWithAppUser(status);
  }
}
