package com.example.et.controller.dto.dashboard;

public record CategoryBreakdownDto(
    String categoryName,
    Double totalAmount,
    Double percentage,
    Long transactionCount
) {}