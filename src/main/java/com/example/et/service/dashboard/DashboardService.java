package com.example.et.service.dashboard;

import com.example.et.controller.dto.CategoryBreakdownDto;
import com.example.et.controller.dto.DashboardSummaryDto;
import com.example.et.controller.dto.OnboardUserDto;

import java.util.List;

public interface DashboardService {
  OnboardUserDto onboardUser(String userId, OnboardUserDto requestBody);

  List<CategoryBreakdownDto> getCategoryBreakdown(String userId, Integer year, Integer month);

  DashboardSummaryDto getSummary(String userId);
}
