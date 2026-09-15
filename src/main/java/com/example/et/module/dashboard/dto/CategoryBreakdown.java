package com.example.et.module.dashboard.dto;

public record CategoryBreakdown(
    String categoryName,
    Double totalAmount,
    Double percentage,
    Long transactionCount
) {
}