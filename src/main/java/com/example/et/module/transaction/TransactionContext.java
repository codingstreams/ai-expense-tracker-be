package com.example.et.module.transaction;

import com.example.et.module.reference.category.SystemCategory;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDto;
import com.example.et.module.transaction.dto.TransactionRequestDto;

public record TransactionContext(
    String userId,
    TransactionRequestDto requestDto,
    PaymentModeDto paymentMode,
    SystemCategory systemCategory
) {
}
