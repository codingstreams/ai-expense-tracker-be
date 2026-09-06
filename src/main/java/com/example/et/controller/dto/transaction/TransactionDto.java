package com.example.et.controller.dto.transaction;

import com.example.et.controller.dto.account.AccountDto;
import com.example.et.controller.dto.category.SystemCategoryDto;
import com.example.et.controller.dto.paymentmode.PaymentModeDto;
import com.example.et.model.core.Transaction;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionDto(
    UUID id,
    Transaction.TransactionType type,
    Float amount,
    LocalDate transactionDate,
    UUID transferId,
    String description,
    SystemCategoryDto transactionCategory,
    AccountDto account,
    PaymentModeDto paymentMode
) {}
