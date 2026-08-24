package com.example.et.controller.dto;

import java.util.UUID;

public record CategoryBreakdownDto(
    UUID categoryId,
    String categoryName,
    Double totalAmount,
    Double percentage,
    Long transactionCount
) {}
