package com.example.et.controller.dto;

import com.example.et.model.core.Account;
import com.example.et.model.core.Bank;

import java.util.UUID;

public record AccountDto(
    UUID id,
    String lastFourDigits,
    Float balance,
    Account.AccountType accountType,
    Bank bank,
    Boolean isUpiEnabled,
    Boolean isNetBankingEnabled
) {
}
