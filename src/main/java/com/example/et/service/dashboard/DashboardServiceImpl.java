package com.example.et.service.dashboard;

import com.example.et.controller.dto.CategoryBreakdownDto;
import com.example.et.controller.dto.DashboardSummaryDto;
import com.example.et.controller.dto.MonthlyTrendDto;
import com.example.et.model.core.Account;
import com.example.et.model.core.SystemCategory;
import com.example.et.model.core.Transaction;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
  private final AccountRepo accountRepo;
  private final TransactionRepo transactionRepo;

  @Override
  public DashboardSummaryDto getSummary(String userId) {
    final var userUuid = UUID.fromString(userId);
    final var accounts = accountRepo.findByAppUserId(userUuid);

    final var netWorth = accounts.stream()
        .mapToDouble(a -> a.getAccountType() == Account.AccountType.CREDIT ? -a.getBalance() : a.getBalance())
        .sum();

    final var now = LocalDate.now();
    final var startDate = now.withDayOfMonth(1);
    final var endDate = now.withDayOfMonth(now.lengthOfMonth());

    final var transactions = transactionRepo.findByAppUserIdAndTransactionDateBetween(userUuid, startDate, endDate);

    final var totalExpense = transactions.stream()
        .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
        .mapToDouble(t -> Math.abs(t.getAmount()))
        .sum();

    final var totalIncome = transactions.stream()
        .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
        .mapToDouble(t -> Math.abs(t.getAmount()))
        .sum();

    final var netSavings = totalIncome - totalExpense;
    final var dailyBurnRate = now.getDayOfMonth() > 0 ? totalExpense / now.getDayOfMonth() : 0.0;

    return new DashboardSummaryDto(netWorth, totalIncome, totalExpense, netSavings, dailyBurnRate);
  }

  @Override
  public List<CategoryBreakdownDto> getCategoryBreakdown(String userId, Integer year, Integer month) {
    final var now = LocalDate.now();
    final var targetYear = year != null ? year : now.getYear();
    final var targetMonth = month != null ? month : now.getMonthValue();

    final var yearMonth = YearMonth.of(targetYear, targetMonth);
    final var startDate = yearMonth.atDay(1);
    final var endDate = yearMonth.atEndOfMonth();

    final var userUuid = UUID.fromString(userId);
    final var transactions = transactionRepo.findByAppUserIdAndTransactionDateBetween(userUuid, startDate, endDate);

    final var expenseTransactions = transactions.stream()
        .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
        .toList();

    final var totalExpense = expenseTransactions.stream()
        .mapToDouble(t -> Math.abs(t.getAmount()))
        .sum();

    final Map<SystemCategory, List<Transaction>> grouped = expenseTransactions.stream()
        .collect(Collectors.groupingBy(t -> t.getTransactionCategory() != null ? t.getTransactionCategory() : new SystemCategory()));

    return grouped.entrySet().stream()
        .map(entry -> {
          final var category = entry.getKey();
          final var txList = entry.getValue();
          final var sum = txList.stream().mapToDouble(t -> Math.abs(t.getAmount())).sum();
          final var percentage = totalExpense > 0 ? (sum / totalExpense) * 100.0 : 0.0;
          return new CategoryBreakdownDto(
              category.getId(),
              category.getName() != null ? category.getName() : "Uncategorized",
              sum,
              percentage,
              (long) txList.size()
          );
        })
        .sorted(Comparator.comparingDouble(CategoryBreakdownDto::totalAmount).reversed())
        .toList();
  }

  @Override
  public List<MonthlyTrendDto> getMonthlyTrend(String userId, Integer months) {
    final var count = (months != null && months > 0) ? months : 6;
    final var now = YearMonth.now();
    final var startYearMonth = now.minusMonths(count - 1);

    final var userUuid = UUID.fromString(userId);
    final var transactions = transactionRepo.findByAppUserIdAndTransactionDateBetween(
        userUuid, startYearMonth.atDay(1), now.atEndOfMonth());

    final var result = new ArrayList<MonthlyTrendDto>();

    for (int i = 0; i < count; i++) {
      final var currentYearMonth = startYearMonth.plusMonths(i);
      final var monthStart = currentYearMonth.atDay(1);
      final var monthEnd = currentYearMonth.atEndOfMonth();

      final var monthTxns = transactions.stream()
          .filter(t -> !t.getTransactionDate().isBefore(monthStart) && !t.getTransactionDate().isAfter(monthEnd))
          .toList();

      final var totalIncome = monthTxns.stream()
          .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
          .mapToDouble(t -> Math.abs(t.getAmount()))
          .sum();

      final var totalExpense = monthTxns.stream()
          .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
          .mapToDouble(t -> Math.abs(t.getAmount()))
          .sum();

      final var netSavings = totalIncome - totalExpense;

      result.add(new MonthlyTrendDto(
          currentYearMonth.getMonth().name(),
          currentYearMonth.getYear(),
          currentYearMonth.getMonthValue(),
          totalIncome,
          totalExpense,
          netSavings
      ));
    }

    return result;
  }
}
