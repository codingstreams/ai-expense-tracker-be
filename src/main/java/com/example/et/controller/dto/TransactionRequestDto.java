package com.example.et.controller.dto;

import com.example.et.model.core.Transaction;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionRequestDto(
    UUID id,
    Transaction.TransactionType type,
    Float amount,
    LocalDate transactionDate,
    String description,
    UUID accountId,
    UUID cardId,
    UUID toAccountId,
    UUID paymentModeId,
    UUID categoryId,
    UUID transferId
) {
}