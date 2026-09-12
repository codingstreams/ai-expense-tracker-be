package com.example.et.controller.dto.dashboard;

public record UserSummaryDto(
    Double netWorth,
    Double totalIncome,
    Double totalExpense,
    Double netSavings,
    Double dailyBurnRate
) {
  public static UserSummaryDto empty() {
    return new UserSummaryDto(0.0, 0.0, 0.0, 0.0, 0.0);
  }
}