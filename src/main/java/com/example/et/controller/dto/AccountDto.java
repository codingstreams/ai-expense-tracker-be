package com.example.et.controller.dto;

import com.example.et.model.core.Account;
import com.example.et.model.core.Bank;

import java.math.BigDecimal;

public record AccountDto(String id, String lastFourDigits, BigDecimal balance, Account.AccountType accountType, Bank bank) {
}
