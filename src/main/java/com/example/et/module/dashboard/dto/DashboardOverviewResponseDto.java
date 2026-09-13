package com.example.et.module.dashboard.dto;

import com.example.et.module.transaction.dto.TransactionDetailsResponse;

import java.util.List;

public record DashboardOverviewResponseDto(
    UserSummaryDto userSummary, List<MonthlyTrendDto> monthlyTrend,
    List<TransactionDetailsResponse> recentTransactions, List<CategoryBreakdownDto> categoryBreakdown) {
}
