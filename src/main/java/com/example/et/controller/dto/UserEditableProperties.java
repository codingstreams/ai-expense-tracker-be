package com.example.et.controller.dto;

import com.example.et.model.core.AppUserConfig;
import com.example.et.model.core.LanguagePreference;

import java.math.BigDecimal;

public interface UserEditableProperties {
    LanguagePreference languagePreference();
    BigDecimal spendLimit();
    AppUserConfig.Currency currency();
    String paymentMode();
}