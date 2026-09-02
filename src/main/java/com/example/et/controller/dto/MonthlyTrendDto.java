package com.example.et.controller.dto;

public record MonthlyTrendDto(
    String month,
    int year,
    int monthValue,
    Double totalIncome,
    Double totalExpense,
    Double netSavings
) {}
