package com.example.et.controller.dto.dashboard;

public record UserSummaryDto(
    Double netWorth,
    Double totalIncome,
    Double totalExpense,
    Double netSavings,
    Double dailyBurnRate
) {}