package com.example.et.module.dashboard.dto;

public record MonthlyTrendDto(
    String month,
    int year,
    int monthValue,
    Double totalIncome,
    Double totalExpense,
    Double netSavings
) {
}
