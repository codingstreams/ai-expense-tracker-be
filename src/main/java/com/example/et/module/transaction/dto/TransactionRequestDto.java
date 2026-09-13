package com.example.et.module.transaction.dto;

import com.example.et.module.transaction.Transaction;

import java.time.LocalDate;

public record TransactionRequestDto(
    String id,
    Transaction.TransactionType type,
    Float amount,
    LocalDate transactionDate,
    String description,
    String accountId,
    String cardId,
    String toAccountId,
    String paymentModeId,
    String categoryId,
    String transferId
) {
}