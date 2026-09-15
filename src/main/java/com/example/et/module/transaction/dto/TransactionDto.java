package com.example.et.module.transaction.dto;

import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.reference.category.dto.CategoryDetailsResponse;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDetailsResponse;
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
    CategoryDetailsResponse transactionCategory,
    AccountDetailsResponse account,
    PaymentModeDetailsResponse paymentMode
) {
}
