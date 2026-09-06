package com.example.et.controller.dto.account;

import com.example.et.model.core.Account;
import com.example.et.model.core.Bank;

import java.util.UUID;

public record AccountDtoOld(
    UUID id,
    String lastFourDigits,
    Float balance,
    Account.AccountType accountType,
    Bank bank,
    Boolean isUpiEnabled,
    Boolean isNetBankingEnabled
) {
}
