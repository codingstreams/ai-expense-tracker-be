package com.example.et.controller.dto;

import com.example.et.model.core.AppUserConfig;

public record UpdateUserDetailsDto(AppUserConfig.LanguagePreference languagePreference,
                                   Integer spendLimit,
                                   AppUserConfig.Currency currency,  String paymentMode, Boolean isOnboardingComplete) {
}
