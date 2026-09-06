package com.example.et.controller.dto.paymentmode;

import java.util.UUID;

public record PaymentModeSummaryDto(
    UUID id,
    String name
) {}