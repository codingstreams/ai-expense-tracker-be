package com.example.et.controller.dto.appuser;

import com.example.et.model.core.AppUserConfig;

public record UpdateUserConfigReq(AppUserConfig.LanguagePreference languagePreference,
                                  Integer spendLimit,
                                  AppUserConfig.Currency currency, String paymentModeId, Boolean isOnboardingComplete) {
}
