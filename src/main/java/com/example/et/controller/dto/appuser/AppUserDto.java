package com.example.et.controller.dto.appuser;

public record AppUserDto(
    String name,
    String email,
    boolean onboardingComplete,
    AppUserConfigDto appUserConfig
) {}