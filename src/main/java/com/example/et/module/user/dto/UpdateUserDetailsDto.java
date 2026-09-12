package com.example.et.module.user.dto;

import com.example.et.module.user.AppUserConfig;

public record UpdateUserDetailsDto(AppUserConfig.LanguagePreference languagePreference,
                                   Integer spendLimit,
                                   AppUserConfig.Currency currency, String paymentMode, Boolean isOnboardingComplete) {
}
