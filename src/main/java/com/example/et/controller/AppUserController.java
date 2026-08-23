package com.example.et.controller;

import com.example.et.model.core.AppUser;
import com.example.et.service.appuser.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {
  private final AppUserService appUserService;

  @GetMapping("/me")
  public AppUser getCurrentUserDetails(@AuthenticationPrincipal String userId) {
    final var appUser = appUserService.getUserByUserId(userId);
    return appUser;
  }
}
