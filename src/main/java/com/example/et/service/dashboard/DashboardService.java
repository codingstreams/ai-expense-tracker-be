package com.example.et.service.dashboard;

import com.example.et.controller.dto.CategoryBreakdownDto;
import com.example.et.controller.dto.DashboardSummaryDto;
import com.example.et.controller.dto.MonthlyTrendDto;
import com.example.et.controller.dto.OnboardUserDto;

import java.util.List;

public interface DashboardService {
  DashboardSummaryDto getSummary(String userId);

  List<CategoryBreakdownDto> getCategoryBreakdown(String userId, Integer year, Integer month);

  List<MonthlyTrendDto> getMonthlyTrend(String userId, Integer months);

  OnboardUserDto onboardUser(String userId, OnboardUserDto requestBody);
}
