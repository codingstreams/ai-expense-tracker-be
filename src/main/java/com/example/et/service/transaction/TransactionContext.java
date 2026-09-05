package com.example.et.service.transaction;

import com.example.et.controller.dto.transaction.TransactionRequestDto;
import com.example.et.model.core.PaymentMode;
import com.example.et.model.core.SystemCategory;

public record TransactionContext(
    String userId,
    TransactionRequestDto requestDto,
    PaymentMode paymentMode,
    SystemCategory systemCategory
) {
}
