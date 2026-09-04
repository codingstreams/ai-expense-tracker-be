package com.example.et.controller;

import com.example.et.controller.dto.*;
import com.example.et.model.core.AppUserConfig;
import com.example.et.service.dashboard.DashboardService;
import com.example.et.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
  private final DashboardService dashboardService;
  private final TransactionService transactionService;

  @GetMapping(value = "/overview", version = "2")
  public ResponseEntity<DashboardOverviewResponseDto> getDashboardOverview(@AuthenticationPrincipal String userId){
    final var userSummary = dashboardService.getSummary(userId);
    final var monthlyTrend = dashboardService.getMonthlyTrend(userId);
    final var recentTransactions = transactionService.getRecentTransactions(userId);
    final var categoryBreakdown = dashboardService.getCategoryBreakdown(userId, null, null);

    final var result = new DashboardOverviewResponseDto(userSummary, monthlyTrend, recentTransactions, categoryBreakdown);

    return  ResponseEntity.ok(result);
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
  public ResponseEntity<OnboardUserDto> onboardUser(@AuthenticationPrincipal String userId, @RequestBody OnboardUserDto requestBody){
    final var response = dashboardService.onboardUser(userId, requestBody);
    return ResponseEntity.ok(response);
  }
}