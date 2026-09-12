package com.example.et.module.user;


import com.example.et.module.user.dto.AppUserDto;
import com.example.et.module.user.dto.UpdateUserConfigReq;
import com.example.et.module.user.dto.UpdateUserDetailsDto;
import com.example.et.module.user.dto.UserDetailsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {
  private final AppUserService appUserService;

  // Version 1
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

  // Version 2
  @GetMapping(value = "/me", version = "2")
  public ResponseEntity<AppUserDto> getCurrentUserDetailsV2(@AuthenticationPrincipal String userId) {
    final var appUser = appUserService.getUserByUserIdWithConfigV2(userId);
    return ResponseEntity.ok(appUser);
  }

  @PutMapping(value = "/me/config", version = "2")
  public ResponseEntity<AppUserDto> updateUserConfigV2(@AuthenticationPrincipal String userId, @RequestBody UpdateUserConfigReq updateUserConfigReq) {
    final var appUser = appUserService.updateUserConfigV2(userId, updateUserConfigReq);
    return ResponseEntity.ok(appUser);
  }
}
