package com.example.et.service.dashboard;

import com.example.et.controller.dto.*;
import com.example.et.model.core.Account;
import com.example.et.model.core.SystemCategory;
import com.example.et.model.core.Transaction;
import com.example.et.service.account.AccountService;
import com.example.et.service.appuser.AppUserService;
import com.example.et.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
  private final AppUserService appUserService;
  private final AccountService accountService;
  private final TransactionService transactionService;


  @Override
  public OnboardUserDto onboardUser(String userId, OnboardUserDto requestBody) {
    final var accounts = accountService.addAccounts(userId, new UserBankAccounts(requestBody.accounts()));
    final var cashBalance = accountService.updateCashBalance(userId, requestBody.cashBalance());
    final var userConfig = appUserService.updateUserConfig(userId, requestBody.userConfig());

    return new OnboardUserDto(userConfig, cashBalance, accounts);
  }

  @Override
  public List<CategoryBreakdownDto> getCategoryBreakdown(String userId, Integer year, Integer month) {
    final var now = LocalDate.now();
    final var targetYear = year != null ? year : now.getYear();
    final var targetMonth = month != null ? month : now.getMonthValue();

    final var yearMonth = YearMonth.of(targetYear, targetMonth);
    final var startDate = yearMonth.atDay(1);
    final var endDate = yearMonth.atEndOfMonth();

    final var transactions = transactionService.getAllTransactions(userId, TransactionFilterParams.dateRange(startDate, endDate), Pageable.unpaged())
        .content();

    final var expenseTransactions = transactions.stream()
        .filter(t -> t.type() == Transaction.TransactionType.EXPENSE)
        .toList();

    final var totalExpense = expenseTransactions.stream()
        .mapToDouble(t -> Math.abs(t.amount()))
        .sum();

    final Map<String, List<TransactionResponseDto>> grouped = expenseTransactions.stream()
        .collect(Collectors.groupingBy(t -> t.category() != null ? t.category() : "Uncategorized"));

    return grouped.entrySet().stream()
        .map(entry -> {
          final var category = entry.getKey();
          final var txList = entry.getValue();
          final var sum = txList.stream().mapToDouble(t -> Math.abs(t.amount())).sum();
          final var percentage = totalExpense > 0 ? (sum / totalExpense) * 100.0 : 0.0;
          return new CategoryBreakdownDto(
              category != null ? category : "Uncategorized",
              sum,
              percentage,
              (long) txList.size()
          );
        })
        .sorted(Comparator.comparingDouble(CategoryBreakdownDto::totalAmount).reversed())
        .toList();
  }

  @Override
  public DashboardSummaryDto getSummary(String userId) {
    final var accounts = accountService.getUserAccountList(userId);

    // Net worth
    final var netWorth = accounts.stream()
        .mapToDouble(a -> a.getAccountType() == Account.AccountType.CREDIT ? -a.getBalance() : a.getBalance())
        .sum();

    // Total Expense
    final var now = LocalDate.now();
    final var startDate = now.withDayOfMonth(1);
    final var endDate = now.withDayOfMonth(now.lengthOfMonth());

    final var transactions = transactionService.getAllTransactions(userId, TransactionFilterParams.dateRange(startDate, endDate), Pageable.unpaged())
        .content();

    final var totalExpense = transactions.stream()
        .filter(t -> t.type() == Transaction.TransactionType.EXPENSE)
        .mapToDouble(t -> Math.abs(t.amount()))
        .sum();

    // Total Income
    final var totalIncome = transactions.stream()
        .filter(t -> t.type() == Transaction.TransactionType.INCOME)
        .mapToDouble(t -> Math.abs(t.amount()))
        .sum();

    // Daily Burn Rate
    final var netSavings = totalIncome - totalExpense;
    final var dailyBurnRate = totalExpense / now.getDayOfMonth();

    return new DashboardSummaryDto(netWorth, totalIncome, totalExpense, netSavings, dailyBurnRate);
  }

  @Override
  public List<MonthlyTrendDto> getMonthlyTrend(String userId, Integer months) {
    final var count = (months != null && months > 0) ? months : 6;
    final var now = YearMonth.now();
    final var startYearMonth = now.minusMonths(count - 1);

    final var transactions = transactionService.getAllTransactions(
        userId, TransactionFilterParams.dateRange(startYearMonth.atDay(1), now.atEndOfMonth()), Pageable.unpaged()).content();

    final var result = new ArrayList<MonthlyTrendDto>();

    for (int i = 0; i < count; i++) {
      final var currentYearMonth = startYearMonth.plusMonths(i);
      final var monthStart = currentYearMonth.atDay(1);
      final var monthEnd = currentYearMonth.atEndOfMonth();

      final var monthTxns = transactions.stream()
          .filter(t -> !t.transactionDate().isBefore(monthStart) && !t.transactionDate().isAfter(monthEnd))
          .toList();

      final var totalIncome = monthTxns.stream()
          .filter(t -> t.type() == Transaction.TransactionType.INCOME)
          .mapToDouble(t -> Math.abs(t.amount()))
          .sum();

      final var totalExpense = monthTxns.stream()
          .filter(t -> t.type() == Transaction.TransactionType.EXPENSE)
          .mapToDouble(t -> Math.abs(t.amount()))
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
