package com.example.et.controller;

import com.example.et.controller.dto.CategoryBreakdownDto;
import com.example.et.controller.dto.DashboardSummaryDto;
import com.example.et.controller.dto.MonthlyTrendDto;
import com.example.et.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
  private final DashboardService dashboardService;

  @GetMapping("/summary")
  public ResponseEntity<DashboardSummaryDto> getSummary(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(dashboardService.getSummary(userId));
  }

  @GetMapping("/category-breakdown")
  public ResponseEntity<List<CategoryBreakdownDto>> getCategoryBreakdown(
      @RequestParam(required = false) Integer year,
      @RequestParam(required = false) Integer month,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(dashboardService.getCategoryBreakdown(userId, year, month));
  }

  @GetMapping("/monthly-trend")
  public ResponseEntity<List<MonthlyTrendDto>> getMonthlyTrend(
      @RequestParam(required = false, defaultValue = "6") Integer months,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(dashboardService.getMonthlyTrend(userId, months));
  }
}
