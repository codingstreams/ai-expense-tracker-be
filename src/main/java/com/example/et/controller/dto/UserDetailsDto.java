package com.example.et.controller.dto;

import com.example.et.model.core.AppUserConfig;

public record UserDetailsDto(String email,
                            String name,
                            Boolean isOnboardingComplete,
                            AppUserConfig.LanguagePreference languagePreference,
                            Integer spendLimit,
                            AppUserConfig.Currency currency) {
}
