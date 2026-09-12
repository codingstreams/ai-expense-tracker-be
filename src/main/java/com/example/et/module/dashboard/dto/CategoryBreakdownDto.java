package com.example.et.module.dashboard.dto;

public record CategoryBreakdownDto(
    String categoryName,
    Double totalAmount,
    Double percentage,
    Long transactionCount
) {
}