package com.example.et.controller.dto;

public record CategoryBreakdownDto(
    String categoryName,
    Double totalAmount,
    Double percentage,
    Long transactionCount
) {}