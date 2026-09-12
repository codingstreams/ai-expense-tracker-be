package com.example.et.module.dashboard;

import com.example.et.module.dashboard.dto.*;
import com.example.et.module.transaction.TransactionService;
import com.example.et.module.user.AppUserConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
  private final DashboardService dashboardService;
  private final TransactionService transactionService;

  @GetMapping(value = "/overview", version = "2")
  public ResponseEntity<DashboardOverviewResponseDto> getDashboardOverview(@AuthenticationPrincipal String userId) {
    log.info("Fetching dashboard overview for userId: {}", userId);

    final var userSummary = dashboardService.getSummary(userId);
    log.debug("Fetched user summary for userId: {}", userId);

    final var monthlyTrend = dashboardService.getMonthlyTrend(userId);
    log.debug("Fetched monthly trend for userId: {}", userId);

    final var recentTransactions = transactionService.getRecentTransactions(userId);
    log.debug("Fetched recent transactions for userId: {}", userId);

    final var categoryBreakdown = dashboardService.getCategoryBreakdown(userId, null, null);
    log.debug("Fetched category breakdown for userId: {}", userId);

    final var result = new DashboardOverviewResponseDto(userSummary, monthlyTrend, recentTransactions, categoryBreakdown);

    log.info("Successfully generated dashboard overview response for userId: {}", userId);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/language-preferences")
  public ResponseEntity<Map<String, Object>> getLanguagePreferences() {
    final var response = new HashMap<String, Object>();
    response.put("options", AppUserConfig.LanguagePreference.values());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/category-breakdown")
  public ResponseEntity<List<CategoryBreakdownDto>> getCategoryBreakdown(
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) Integer month,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(dashboardService.getCategoryBreakdown(userId, year, month));
  }

  @GetMapping("/summary")
  public ResponseEntity<UserSummaryDto> getSummary(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(dashboardService.getSummary(userId));
  }

  @GetMapping("/monthly-trend")
  public ResponseEntity<List<MonthlyTrendDto>> getMonthlyTrend(
      @RequestParam(required = false, defaultValue = "6") Integer months,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(dashboardService.getMonthlyTrend(userId, months));
  }

  @PostMapping("/onboard-user")
  public ResponseEntity<OnboardUserDto> onboardUser(@AuthenticationPrincipal String userId, @RequestBody OnboardUserDto requestBody) {
    final var response = dashboardService.onboardUser(userId, requestBody);
    return ResponseEntity.ok(response);
  }
}