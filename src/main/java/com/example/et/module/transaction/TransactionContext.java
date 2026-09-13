package com.example.et.module.transaction;

import com.example.et.module.reference.category.SystemCategory;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDetailsResponse;
import com.example.et.module.transaction.dto.CreateTransactionRequest;

public record TransactionContext(
    String userId,
    CreateTransactionRequest requestDto,
    PaymentModeDetailsResponse paymentMode,
    SystemCategory systemCategory
) {
}
