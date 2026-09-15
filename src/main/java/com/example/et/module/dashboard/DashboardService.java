package com.example.et.module.dashboard;

import com.example.et.module.dashboard.dto.CategoryBreakdownResponse;
import com.example.et.module.dashboard.dto.MonthlyTrendDto;
import com.example.et.module.dashboard.dto.OnboardUserDto;
import com.example.et.module.dashboard.dto.UserSummaryDto;

import java.util.List;

public interface DashboardService {
  OnboardUserDto onboardUser(String userId, OnboardUserDto requestBody);

  CategoryBreakdownResponse getCategoryBreakdown(String userId, Integer year, Integer month);

  UserSummaryDto getSummary(String userId);

  List<MonthlyTrendDto> getMonthlyTrend(String userId, Integer months);

  List<MonthlyTrendDto> getMonthlyTrend(String userId);
}
