package com.example.et.controller;

import com.example.et.controller.dto.CategoryBreakdownDto;
import com.example.et.controller.dto.DashboardSummaryDto;
import com.example.et.controller.dto.MonthlyTrendDto;
import com.example.et.controller.dto.OnboardUserDto;
import com.example.et.model.core.AppUserConfig;
import com.example.et.service.dashboard.DashboardService;
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
  public ResponseEntity<DashboardSummaryDto> getSummary(@AuthenticationPrincipal String userId) {
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