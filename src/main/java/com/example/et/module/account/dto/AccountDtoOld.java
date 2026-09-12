package com.example.et.module.account.dto;

import com.example.et.module.account.Account;
import com.example.et.module.reference.bank.Bank;

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
