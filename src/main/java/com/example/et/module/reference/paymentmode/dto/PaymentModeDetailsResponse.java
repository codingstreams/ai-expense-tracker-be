package com.example.et.module.reference.paymentmode.dto;

import java.util.UUID;

public record PaymentModeDetailsResponse(
    UUID id,
    String name
) {
}