package com.example.et.service.dashboard;

import com.example.et.controller.dto.CategoryBreakdownDto;
import com.example.et.controller.dto.UserSummaryDto;
import com.example.et.controller.dto.MonthlyTrendDto;
import com.example.et.controller.dto.OnboardUserDto;

import java.util.List;

public interface DashboardService {
  OnboardUserDto onboardUser(String userId, OnboardUserDto requestBody);

  List<CategoryBreakdownDto> getCategoryBreakdown(String userId, Integer year, Integer month);

  UserSummaryDto getSummary(String userId);

  List<MonthlyTrendDto> getMonthlyTrend(String userId, Integer months);

  List<MonthlyTrendDto> getMonthlyTrend(String userId);
}
