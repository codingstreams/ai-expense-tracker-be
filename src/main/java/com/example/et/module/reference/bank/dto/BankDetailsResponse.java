package com.example.et.module.reference.bank.dto;

import java.util.UUID;

public record BankDetailsResponse(
    UUID id,
    String name
) {
}