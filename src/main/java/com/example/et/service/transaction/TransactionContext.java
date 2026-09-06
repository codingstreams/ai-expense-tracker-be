package com.example.et.service.transaction;

import com.example.et.controller.dto.paymentmode.PaymentModeDto;
import com.example.et.controller.dto.transaction.TransactionRequestDto;
import com.example.et.model.core.SystemCategory;

public record TransactionContext(
    String userId,
    TransactionRequestDto requestDto,
    PaymentModeDto paymentMode,
    SystemCategory systemCategory
) {
}
