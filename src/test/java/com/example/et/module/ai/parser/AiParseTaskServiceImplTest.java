package com.example.et.module.ai.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiParseTaskServiceImplTest {

  @Mock
  private AiParseTaskRepo aiParseTaskRepo;

  @InjectMocks
  private AiParseTaskServiceImpl aiParseTaskService;

  @Test
  void save_ShouldSaveAndReturnTask() {
    AiParsingTask task = AiParsingTask.builder()
        .id(UUID.randomUUID())
        .rawInput("sample input")
        .status(AiParsingTask.Status.PENDING)
        .build();

    when(aiParseTaskRepo.save(task)).thenReturn(task);

    AiParsingTask saved = aiParseTaskService.save(task);

    assertNotNull(saved);
    assertEquals(task.getId(), saved.getId());
    verify(aiParseTaskRepo, times(1)).save(task);
  }

  @Test
  void getPendingTasksWithAppUser_ShouldReturnPendingTasks() {
    AiParsingTask task = AiParsingTask.builder()
        .id(UUID.randomUUID())
        .status(AiParsingTask.Status.PENDING)
        .build();

    when(aiParseTaskRepo.findByStatusWithAppUser(AiParsingTask.Status.PENDING)).thenReturn(List.of(task));

    List<AiParsingTask> results = aiParseTaskService.getPendingTasksWithAppUser(AiParsingTask.Status.PENDING);

    assertNotNull(results);
    assertEquals(1, results.size());
    verify(aiParseTaskRepo, times(1)).findByStatusWithAppUser(AiParsingTask.Status.PENDING);
  }

  @Test
  void getById_ShouldReturnTask_WhenExists() {
    UUID id = UUID.randomUUID();
    AiParsingTask task = AiParsingTask.builder().id(id).build();

    when(aiParseTaskRepo.findById(id)).thenReturn(Optional.of(task));

    AiParsingTask result = aiParseTaskService.getById(id);

    assertNotNull(result);
    assertEquals(id, result.getId());
    verify(aiParseTaskRepo, times(1)).findById(id);
  }

  @Test
  void getById_ShouldReturnNull_WhenNotExists() {
    UUID id = UUID.randomUUID();

    when(aiParseTaskRepo.findById(id)).thenReturn(Optional.empty());

    AiParsingTask result = aiParseTaskService.getById(id);

    assertNull(result);
  }

  @Test
  void unlinkTransaction_ShouldCallRepoUnlink() {
    UUID txId = UUID.randomUUID();

    doNothing().when(aiParseTaskRepo).unlinkTransaction(txId);

    aiParseTaskService.unlinkTransaction(txId);

    verify(aiParseTaskRepo, times(1)).unlinkTransaction(txId);
  }
}
