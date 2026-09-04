package com.example.et.controller.dto;

import java.util.List;

public record DashboardOverviewResponseDto(
    UserSummaryDto userSummary, List<MonthlyTrendDto> monthlyTrend,
    List<TransactionResponseDto> recentTransactions, List<CategoryBreakdownDto> categoryBreakdown){
}
