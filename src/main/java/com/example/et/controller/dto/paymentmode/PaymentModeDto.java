package com.example.et.controller.dto.paymentmode;

import java.util.UUID;

public record PaymentModeDto(
    UUID id,
    String name
) {}