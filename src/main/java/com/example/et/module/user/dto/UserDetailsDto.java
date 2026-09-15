package com.example.et.module.user.dto;

import com.example.et.module.user.AppUserConfig;

public record UserDetailsDto(String email,
                             String name,
                             Boolean isOnboardingComplete,
                             AppUserConfig.LanguagePreference languagePreference,
                             Integer spendLimit,
                             AppUserConfig.Currency currency,
                             String paymentMode) {
}
