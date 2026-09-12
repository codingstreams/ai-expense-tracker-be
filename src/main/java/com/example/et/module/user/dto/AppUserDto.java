package com.example.et.module.user.dto;

public record AppUserDto(
    String name,
    String email,
    boolean onboardingComplete,
    AppUserConfigDto appUserConfig
) {
}