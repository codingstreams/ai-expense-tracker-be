package com.example.et.controller.dto;

import com.example.et.model.core.Account;
import com.example.et.model.core.Bank;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDto(UUID id, String lastFourDigits, BigDecimal balance, Account.AccountType accountType,
                         Bank bank) {
}
