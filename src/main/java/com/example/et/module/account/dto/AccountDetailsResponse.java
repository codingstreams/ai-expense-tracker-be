package com.example.et.module.account.dto;

import com.example.et.module.account.Account;
import com.example.et.module.reference.bank.dto.BankDetailsResponse;

import java.util.UUID;

public record AccountDetailsResponse(
    UUID id,
    String lastFourDigits,
    Float balance,
    Account.AccountType accountType,
    Boolean upiEnabled,
    Boolean netBankingEnabled,
    BankDetailsResponse bank,
    Boolean isActive
) {
}