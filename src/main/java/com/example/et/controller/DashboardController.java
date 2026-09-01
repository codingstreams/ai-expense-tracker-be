package com.example.et.controller;

import com.example.et.controller.dto.OnboardUserDto;
import com.example.et.model.core.AppUserConfig;
import com.example.et.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

  @PostMapping("/onboard-user")
  public ResponseEntity<OnboardUserDto> onboardUser(@AuthenticationPrincipal String userId, @RequestBody OnboardUserDto requestBody){
    final var response = dashboardService.onboardUser(userId, requestBody);
    return ResponseEntity.ok(response);
  }
}