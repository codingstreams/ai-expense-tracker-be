package com.example.et.controller;


import com.example.et.controller.dto.UpdateUserDetailsDto;
import com.example.et.controller.dto.UserDetailsDto;
import com.example.et.service.appuser.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {
  private final AppUserService appUserService;

  @GetMapping("/me")
  public ResponseEntity<UserDetailsDto> getCurrentUserDetails(@AuthenticationPrincipal String userId) {
    final var appUser = appUserService.getUserByUserIdWithConfig(userId);
    return ResponseEntity.ok(appUser);
  }

  @PutMapping("/me/config")
  public ResponseEntity<UpdateUserDetailsDto> updateUserConfig(@AuthenticationPrincipal String userId, @RequestBody UpdateUserDetailsDto userDetailsDto) {
    final var appUser = appUserService.updateUserConfig(userId, userDetailsDto);
    return ResponseEntity.ok(appUser);
  }
}
