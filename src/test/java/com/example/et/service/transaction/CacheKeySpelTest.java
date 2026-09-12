package com.example.et.service.transaction;

import com.example.et.controller.dto.transaction.TransactionFilterParams;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CacheKeySpelTest {

  private static final String SPEL_EXPRESSION =
      "#userId + ':' + (#filterParams != null ? #filterParams.toCacheKey() : 'all') + ':' + (#pageable.isPaged() ? (#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort) : 'unpaged')";
  private final SpelExpressionParser parser = new SpelExpressionParser();

  @Test
  void testUserTransactionsCacheKeySpel_WithPagedAndFilters() {
    // Given parameters
    String userId = "123e4567-e89b-12d3-a456-426614174000";
    TransactionFilterParams filterParams = new TransactionFilterParams(
        "EXPENSE",
        "FOOD",
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 31),
        10.0f,
        500.0f
    );
    Pageable pageable = PageRequest.of(0, 10);

    EvaluationContext context = new StandardEvaluationContext();
    context.setVariable("userId", userId);
    context.setVariable("filterParams", filterParams);
    context.setVariable("pageable", pageable);

    // When
    Expression expression = parser.parseExpression(SPEL_EXPRESSION);
    Object evaluatedKey = expression.getValue(context);

    // Then
    String expectedKey = userId + ":" + filterParams.toCacheKey() + ":0:10:UNSORTED";
    assertNotNull(evaluatedKey);
    assertEquals(expectedKey, evaluatedKey.toString());
  }

  @Test
  void testUserTransactionsCacheKeySpel_WithUnpagedAndNullFilter() {
    String userId = "123e4567-e89b-12d3-a456-426614174000";
    Pageable pageable = Pageable.unpaged();

    EvaluationContext context = new StandardEvaluationContext();
    context.setVariable("userId", userId);
    context.setVariable("filterParams", null);
    context.setVariable("pageable", pageable);

    Expression expression = parser.parseExpression(SPEL_EXPRESSION);
    Object evaluatedKey = expression.getValue(context);

    String expectedKey = userId + ":all:unpaged";
    assertNotNull(evaluatedKey);
    assertEquals(expectedKey, evaluatedKey.toString());
  }
}