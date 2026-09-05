package com.example.et.controller.dto.dashboard;

public record MonthlyTrendDto(
    String month,
    int year,
    int monthValue,
    Double totalIncome,
    Double totalExpense,
    Double netSavings
) {}
