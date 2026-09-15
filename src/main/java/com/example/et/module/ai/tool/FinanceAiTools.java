package com.example.et.module.ai.tool;

import com.example.et.module.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinanceAiTools {
  private static final String CATEGORY_HASH_PREFIX = "summary:category:";
  private static final Duration CACHE_TTL = Duration.ofDays(7);

  private final DashboardService dashboardService;
  private final StringRedisTemplate redisTemplate;

  @Tool(description = "Get category-wise spending totals for a given month and year. Can optionally filter by category name.")
  public String getCategorySpendingSummary(
      @ToolParam(description = "The 4-digit year, e.g. 2026. Defaults to current year if omitted.", required = false) Integer year,
      @ToolParam(description = "The month number (1 to 12). Defaults to current month if omitted.", required = false) Integer month,
      @ToolParam(description = "Optional category name to check, e.g. 'Groceries' or 'Dining'. If omitted, returns all categories.", required = false) String category,
      ToolContext toolContext
  ) {
    final String userId = resolveUserId(toolContext);
    if (userId == null || userId.isBlank()) {
      return "Unable to identify authenticated user.";
    }

    final LocalDate now = LocalDate.now();
    final int targetYear = (year != null && year > 0) ? year : now.getYear();
    final int targetMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();

    final String hashKey = CATEGORY_HASH_PREFIX + userId + ":" + targetYear + "-" + targetMonth;

    if (Boolean.FALSE.equals(redisTemplate.hasKey(hashKey))) {
      populateCategoryHash(userId, targetYear, targetMonth, hashKey);
    }

    final Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);
    if (entries.isEmpty()) {
      return String.format("No spending recorded for %d-%02d.", targetYear, targetMonth);
    }

    if (category != null && !category.isBlank()) {
      final String search = category.trim().toLowerCase();
      for (var entry : entries.entrySet()) {
        final String catName = entry.getKey().toString();
        if (catName.equalsIgnoreCase(search) || catName.toLowerCase().contains(search)) {
          return String.format("%s: $%.2f for %d-%02d", catName, Double.parseDouble(entry.getValue().toString()), targetYear, targetMonth);
        }
      }
      return String.format("No spending recorded in category '%s' for %d-%02d.", category, targetYear, targetMonth);
    }

    final String total = entries.containsKey("_TOTAL_EXPENSE_") ? entries.get("_TOTAL_EXPENSE_").toString() : "0.00";
    final String categoryDetails = entries.entrySet().stream()
        .filter(e -> !e.getKey().toString().startsWith("_"))
        .map(e -> String.format("%s: $%.2f", e.getKey(), Double.parseDouble(e.getValue().toString())))
        .collect(Collectors.joining(" | "));

    if (categoryDetails.isBlank()) {
      return String.format("No spending recorded for %d-%02d.", targetYear, targetMonth);
    }

    return String.format("Month: %d-%02d | %s | Total Expenses: $%s", targetYear, targetMonth, categoryDetails, total);
  }

  @Tool(description = "Get the user's high-level financial health summary (total income, total expenses, net savings, and savings rate) for the current month. Use this to answer queries like total spend vs income, or how much is left over.")
  public String getUserFinancialSummary(
      ToolContext toolContext
  ) {
    final String userId = resolveUserId(toolContext);
    if (userId == null || userId.isBlank()) {
      return "Unable to identify authenticated user.";
    }

    try {
      final var userFinancialSummary = dashboardService.getSummary(userId);
      if (userFinancialSummary == null) {
        return "Unable to fetch user's financial summary.";
      }
      return userFinancialSummary.getFormattedSummary();
    } catch (Exception e) {
      log.error("Failed to fetch financial summary for user: {}", userId, e);
      return "Unable to fetch user's financial summary.";
    }
  }

  private void populateCategoryHash(String userId, int year, int month, String hashKey) {
    try {
      final var breakdown = dashboardService.getCategoryBreakdown(userId, year, month);
      final Map<String, String> map = new HashMap<>();
      double total = 0.0;

      if (breakdown != null) {
        for (var item : breakdown.content()) {
          map.put(item.categoryName(), String.format("%.2f", item.totalAmount()));
          total += item.totalAmount();
        }
      }
      map.put("_TOTAL_EXPENSE_", String.format("%.2f", total));

      redisTemplate.opsForHash().putAll(hashKey, map);
      redisTemplate.expire(hashKey, CACHE_TTL);
      log.debug("Populated category hash in Redis for key: {}", hashKey);
    } catch (Exception e) {
      log.error("Failed to populate category hash for user: {}, year: {}, month: {}", userId, year, month, e);
    }
  }

  private String resolveUserId(ToolContext toolContext) {
    if (toolContext != null) {
      Object userId = toolContext.getContext().get("userId");
      if (userId != null) {
        return userId.toString();
      }
    }
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
      return Objects.requireNonNull(auth.getPrincipal()).toString();
    }
    return null;
  }
}
