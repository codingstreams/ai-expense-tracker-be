package com.example.et.controller.dto.account;

import com.example.et.controller.dto.bank.BankDto;
import com.example.et.model.core.Account;

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
) {}