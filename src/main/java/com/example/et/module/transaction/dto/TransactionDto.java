package com.example.et.module.transaction.dto;

import com.example.et.module.account.dto.AccountDto;
import com.example.et.module.reference.category.dto.SystemCategoryDto;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDto;
import com.example.et.module.transaction.Transaction;

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
) {
}
