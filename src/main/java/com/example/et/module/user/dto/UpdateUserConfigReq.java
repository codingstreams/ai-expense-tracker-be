package com.example.et.module.user.dto;

import com.example.et.module.user.AppUserConfig;

public record UpdateUserConfigReq(AppUserConfig.LanguagePreference languagePreference,
                                  Integer spendLimit,
                                  AppUserConfig.Currency currency, String paymentModeId, Boolean isOnboardingComplete) {
}
