package com.example.et.controller.dto;

public record DashboardSummaryDto(
    Double netWorth,
    Double totalIncome,
    Double totalExpense,
    Double netSavings,
    Double dailyBurnRate
) {}
