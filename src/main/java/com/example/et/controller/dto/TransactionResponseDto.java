package com.example.et.controller.dto;

import com.example.et.model.core.Transaction;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponseDto(
    UUID id,
    Transaction.TransactionType type,
    Float amount,
    LocalDate transactionDate,
    String description,
    String account,
    String paymentMode,
    String category
) {
}