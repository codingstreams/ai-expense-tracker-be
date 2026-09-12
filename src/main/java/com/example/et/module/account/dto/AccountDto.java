package com.example.et.module.account.dto;

import com.example.et.module.account.Account;
import com.example.et.module.reference.bank.dto.BankDto;

import java.util.UUID;

public record AccountDto(
    UUID id,
    String lastFourDigits,
    Float balance,
    Account.AccountType accountType,
    Boolean upiEnabled,
    Boolean netBankingEnabled,
    BankDto bank,
    Boolean isActive
) {
}