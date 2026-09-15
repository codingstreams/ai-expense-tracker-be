package com.example.et.module.dashboard.dto;

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

  public Float getSavingsRate() {
    // Savings Rate = (Total Income - Total Expenses) ÷ Total Income × 100
    if (totalIncome == null || totalIncome == 0.0) {
      return Float.NaN;
    }
    double expense = totalExpense != null ? totalExpense : 0.0;
    return (float) (((totalIncome - expense) / totalIncome) * 100.0);
  }

  public String getFormattedSummary() {
    float savingsRate = getSavingsRate();
    String rateStr = Float.isNaN(savingsRate) ? "0.00%" : String.format(java.util.Locale.US, "%.2f%%", savingsRate);
    return String.format(
        java.util.Locale.US,
        "Total Income: ₹%.2f | Total Expense: ₹%.2f | Net Savings: ₹%.2f (%s savings rate)",
        totalIncome != null ? totalIncome : 0.0,
        totalExpense != null ? totalExpense : 0.0,
        netSavings != null ? netSavings : 0.0,
        rateStr
    );
  }
}