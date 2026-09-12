package com.example.et.module.user.dto;

import com.example.et.module.reference.paymentmode.dto.PaymentModeDto;
import com.example.et.module.user.AppUserConfig;

import java.util.UUID;

public record AppUserConfigDto(
    UUID id,
    AppUserConfig.LanguagePreference languagePreference,
    Integer spendLimit,
    AppUserConfig.Currency currency,
    PaymentModeDto paymentMode
) {
}