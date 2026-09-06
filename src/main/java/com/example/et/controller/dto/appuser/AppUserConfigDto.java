package com.example.et.controller.dto.appuser;

import com.example.et.controller.dto.paymentmode.PaymentModeDto;
import com.example.et.model.core.AppUserConfig;

import java.util.UUID;

public record AppUserConfigDto(
    UUID id,
    AppUserConfig.LanguagePreference languagePreference,
    Integer spendLimit,
    AppUserConfig.Currency currency,
    PaymentModeDto paymentMode
) {}