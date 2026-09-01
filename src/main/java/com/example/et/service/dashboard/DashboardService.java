package com.example.et.service.dashboard;

import com.example.et.controller.dto.OnboardUserDto;

public interface DashboardService {
  OnboardUserDto onboardUser(String userId, OnboardUserDto requestBody);
}
