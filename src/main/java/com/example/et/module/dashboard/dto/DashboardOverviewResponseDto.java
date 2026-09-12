package com.example.et.module.dashboard.dto;

import com.example.et.module.transaction.dto.TransactionResponseDto;

import java.util.List;

public record DashboardOverviewResponseDto(
    UserSummaryDto userSummary, List<MonthlyTrendDto> monthlyTrend,
    List<TransactionResponseDto> recentTransactions, List<CategoryBreakdownDto> categoryBreakdown) {
}
