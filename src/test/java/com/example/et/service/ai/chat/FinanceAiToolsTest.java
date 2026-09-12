package com.example.et.service.ai.chat;

import com.example.et.module.ai.tool.FinanceAiTools;
import com.example.et.module.dashboard.DashboardService;
import com.example.et.module.dashboard.dto.CategoryBreakdownDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceAiToolsTest {

  @Mock
  private DashboardService dashboardService;

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private HashOperations<String, Object, Object> hashOperations;

  private FinanceAiTools financeAiTools;

  @BeforeEach
  void setUp() {
    financeAiTools = new FinanceAiTools(dashboardService, redisTemplate);
  }

  @Test
  void getCategorySpendingSummary_cacheMiss_populatesAndReturnsBreakdown() {
    final String userId = "user-123";
    final String hashKey = "summary:category:user-123:2026-9";
    final ToolContext toolContext = new ToolContext(Map.of("userId", userId));

    when(redisTemplate.hasKey(hashKey)).thenReturn(false);
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);

    when(dashboardService.getCategoryBreakdown(userId, 2026, 9)).thenReturn(List.of(
        new CategoryBreakdownDto("Groceries", 150.50, 60.0, 5L),
        new CategoryBreakdownDto("Dining", 100.00, 40.0, 3L)
    ));

    final Map<Object, Object> populatedEntries = new HashMap<>();
    populatedEntries.put("Groceries", "150.50");
    populatedEntries.put("Dining", "100.00");
    populatedEntries.put("_TOTAL_EXPENSE_", "250.50");
    when(hashOperations.entries(hashKey)).thenReturn(populatedEntries);

    String result = financeAiTools.getCategorySpendingSummary(2026, 9, null, toolContext);

    assertTrue(result.contains("Groceries: $150.50"));
    assertTrue(result.contains("Dining: $100.00"));
    assertTrue(result.contains("Total Expenses: $250.50"));

    verify(dashboardService).getCategoryBreakdown(userId, 2026, 9);
    verify(hashOperations).putAll(eq(hashKey), anyMap());
    verify(redisTemplate).expire(hashKey, Duration.ofDays(7));
  }

  @Test
  void getCategorySpendingSummary_cacheHit_specificCategoryFilter() {
    final String userId = "user-123";
    final String hashKey = "summary:category:user-123:2026-9";
    final ToolContext toolContext = new ToolContext(Map.of("userId", userId));

    when(redisTemplate.hasKey(hashKey)).thenReturn(true);
    when(redisTemplate.opsForHash()).thenReturn(hashOperations);

    final Map<Object, Object> cachedEntries = new HashMap<>();
    cachedEntries.put("Groceries", "210.00");
    cachedEntries.put("Utilities", "80.50");
    cachedEntries.put("_TOTAL_EXPENSE_", "290.50");
    when(hashOperations.entries(hashKey)).thenReturn(cachedEntries);

    String result = financeAiTools.getCategorySpendingSummary(2026, 9, "groceries", toolContext);

    assertEquals("Groceries: $210.00 for 2026-09", result);
    verify(dashboardService, never()).getCategoryBreakdown(anyString(), anyInt(), anyInt());
  }

  @Test
  void getCategorySpendingSummary_unauthenticated_returnsErrorMessage() {
    ToolContext emptyContext = new ToolContext(Collections.emptyMap());

    String result = financeAiTools.getCategorySpendingSummary(2026, 9, null, emptyContext);

    assertEquals("Unable to identify authenticated user.", result);
    verify(redisTemplate, never()).hasKey(anyString());
  }
}
