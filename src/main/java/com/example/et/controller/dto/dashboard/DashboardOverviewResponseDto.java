package com.example.et.controller.dto.dashboard;

import com.example.et.controller.dto.transaction.TransactionResponseDto;

import java.util.List;

public record DashboardOverviewResponseDto(
    UserSummaryDto userSummary, List<MonthlyTrendDto> monthlyTrend,
    List<TransactionResponseDto> recentTransactions, List<CategoryBreakdownDto> categoryBreakdown){
}
