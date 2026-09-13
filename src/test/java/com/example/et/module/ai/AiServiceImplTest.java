package com.example.et.module.ai;

import com.example.et.module.account.AccountService;
import com.example.et.module.ai.dto.AiInputDto;
import com.example.et.module.ai.dto.AiInsightDto;
import com.example.et.module.ai.dto.AiTaskDto;
import com.example.et.module.ai.insight.AiInsight;
import com.example.et.module.ai.insight.AiInsightRepo;
import com.example.et.module.ai.internal.AiServiceImpl;
import com.example.et.module.ai.parser.AiParseTaskService;
import com.example.et.module.ai.parser.AiParsingTask;
import com.example.et.module.reference.category.internal.SysCategoryRepo;
import com.example.et.module.reference.paymentmode.internal.PaymentModeRepo;
import com.example.et.module.transaction.TransactionService;
import com.example.et.module.transaction.dto.PagedTransactionsDto;
import com.example.et.module.user.AppUser;
import com.example.et.module.user.internal.AppUserConfigRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

  @Mock
  private AiParseTaskService aiParseTaskService;

  @Mock
  private ChatClient chatClient;

  @Mock
  private PaymentModeRepo paymentModeRepo;

  @Mock
  private AppUserConfigRepo appUserConfigRepo;

  @Mock
  private SysCategoryRepo sysCategoryRepo;

  @Mock
  private AccountService accountService;

  @Mock
  private TransactionService transactionService;

  @Mock
  private AiInsightRepo aiInsightRepo;

  @InjectMocks
  private AiServiceImpl aiService;

  private UUID userUuid;
  private String userId;

  @BeforeEach
  void setUp() {
    userUuid = UUID.randomUUID();
    userId = userUuid.toString();
  }

  // ----------------------------------------------------------------------
  // save
  // ----------------------------------------------------------------------
  @Test
  void save_ShouldCreateAndSaveAiParsingTask() {
    AiInputDto inputDto = new AiInputDto("Paid 250 for lunch at restaurant via UPI");
    UUID taskId = UUID.randomUUID();

    AiParsingTask savedTask = AiParsingTask.builder()
        .id(taskId)
        .appUser(AppUser.ofId(userId))
        .rawInput(inputDto.rawText())
        .status(AiParsingTask.Status.PENDING)
        .build();

    when(aiParseTaskService.save(any(AiParsingTask.class))).thenReturn(savedTask);

    AiTaskDto result = aiService.save(userId, inputDto);

    assertNotNull(result);
    assertEquals(taskId.toString(), result.id());
    assertEquals("Task created successfully", result.message());

    ArgumentCaptor<AiParsingTask> taskCaptor = ArgumentCaptor.forClass(AiParsingTask.class);
    verify(aiParseTaskService, times(1)).save(taskCaptor.capture());
    AiParsingTask captured = taskCaptor.getValue();
    assertEquals(AiParsingTask.Status.PENDING, captured.getStatus());
    assertEquals(inputDto.rawText(), captured.getRawInput());
  }

  // ----------------------------------------------------------------------
  // getLatestInsight
  // ----------------------------------------------------------------------
  @Test
  void getLatestInsight_ShouldReturnDto_WhenInsightExists() {
    AiInsight insight = AiInsight.builder()
        .id(UUID.randomUUID())
        .appUser(AppUser.ofId(userId))
        .period("September 2026")
        .summary("Good spending habits this month.")
        .topSpendingCategory("Food")
        .topSpendingPercentage(45.0f)
        .topSpendingInsight("Dining out is major expense")
        .anomalies("High coffee spend;Weekend party")
        .actionableTips("Cut down on dining out;Use discounts")
        .build();

    when(aiInsightRepo.findFirstByAppUserIdOrderByCreatedAtDesc(userUuid)).thenReturn(Optional.of(insight));

    AiInsightDto result = aiService.getLatestInsight(userId);

    assertNotNull(result);
    assertEquals("September 2026", result.period());
    assertEquals("Good spending habits this month.", result.summary());
    assertNotNull(result.topSpendingCategory());
    assertEquals("Food", result.topSpendingCategory().category());
    assertEquals(2, result.anomalies().size());
    assertEquals(2, result.actionableTips().size());
  }

  @Test
  void getLatestInsight_ShouldReturnNull_WhenNoInsightExists() {
    when(aiInsightRepo.findFirstByAppUserIdOrderByCreatedAtDesc(userUuid)).thenReturn(Optional.empty());

    AiInsightDto result = aiService.getLatestInsight(userId);

    assertNull(result);
  }

  // ----------------------------------------------------------------------
  // generateInsights rate limits & empty state
  // ----------------------------------------------------------------------
  @Test
  void generateInsights_ShouldThrowException_WhenWeeklyLimitReached() {
    when(aiInsightRepo.countByAppUserIdAndCreatedAtGreaterThanEqual(eq(userUuid), any(LocalDateTime.class)))
        .thenReturn(2L);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> aiService.generateInsights(userId));

    assertTrue(ex.getMessage().contains("Weekly AI insight limit reached"));
    verifyNoInteractions(transactionService);
  }

  @Test
  void generateInsights_ShouldThrowException_WhenMonthlyLimitReached() {
    when(aiInsightRepo.countByAppUserIdAndCreatedAtGreaterThanEqual(eq(userUuid), any(LocalDateTime.class)))
        .thenReturn(1L)
        .thenReturn(4L);

    RuntimeException ex = assertThrows(RuntimeException.class, () -> aiService.generateInsights(userId));

    assertTrue(ex.getMessage().contains("Monthly AI insight limit reached"));
    verifyNoInteractions(transactionService);
  }

  @Test
  void generateInsights_ShouldReturnPlaceholder_WhenNoTransactionsExist() {
    when(aiInsightRepo.countByAppUserIdAndCreatedAtGreaterThanEqual(eq(userUuid), any(LocalDateTime.class)))
        .thenReturn(0L)
        .thenReturn(0L);

    PagedTransactionsDto emptyTxns = new PagedTransactionsDto(Collections.emptyList(), 0, 10, 0, 0, true);
    when(transactionService.getAllTransactions(eq(userId), any(), any())).thenReturn(emptyTxns);

    AiInsightDto result = aiService.generateInsights(userId);

    assertNotNull(result);
    assertEquals("No transactions recorded for the current period.", result.summary());
    assertTrue(result.anomalies().isEmpty());
    assertEquals(1, result.actionableTips().size());
  }

  // ----------------------------------------------------------------------
  // parse failure handling
  // ----------------------------------------------------------------------
  @Test
  void parse_ShouldSetFailed_WhenExceptionOccursDuringParsing() {
    AiParsingTask task = AiParsingTask.builder()
        .id(UUID.randomUUID())
        .appUser(AppUser.ofId(userId))
        .rawInput("invalid gibberish input")
        .status(AiParsingTask.Status.PENDING)
        .build();

    when(chatClient.prompt()).thenThrow(new RuntimeException("LLM service unreachable"));

    aiService.parse(task);

    assertEquals(AiParsingTask.Status.FAILED, task.getStatus());
    assertEquals("LLM service unreachable", task.getErrorMessage());
    verify(aiParseTaskService, times(1)).save(task);
  }
}
