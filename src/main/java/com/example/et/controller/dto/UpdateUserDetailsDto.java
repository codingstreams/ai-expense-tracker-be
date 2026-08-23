package com.example.et.controller.dto;

import com.example.et.model.core.AppUserConfig;
import com.example.et.model.core.LanguagePreference;

public record UpdateUserDetailsDto(LanguagePreference languagePreference,
                                   Integer spendLimit,
                                   AppUserConfig.Currency currency,
                                   String paymentMode) implements UserEditableProperties {
}
