package com.example.et.module.dashboard;

import com.example.et.module.account.Account;
import com.example.et.module.account.AccountService;
import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.dashboard.dto.*;
import com.example.et.module.dashboard.internal.DashboardServiceImpl;
import com.example.et.module.transaction.Transaction;
import com.example.et.module.transaction.TransactionService;
import com.example.et.module.transaction.dto.PagedTransactionsResponse;
import com.example.et.module.transaction.dto.TransactionDetailsResponse;
import com.example.et.module.transaction.dto.TransactionFilterParams;
import com.example.et.module.user.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

  @Mock
  private AppUserService appUserService;

  @Mock
  private AccountService accountService;

  @Mock
  private TransactionService transactionService;

  @InjectMocks
  private DashboardServiceImpl dashboardService;

  private String userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID().toString();
  }

  // ----------------------------------------------------------------------
  // onboardUser
  // ----------------------------------------------------------------------
  @Test
  void onboardUser_ShouldAddAccountsUpdateCashAndUserConfig() {
    AccountDetailsResponse accountDetailsResponse = new AccountDetailsResponse(UUID.randomUUID(), "1111", 1000.0f, Account.AccountType.SAVINGS, true, true, null, true);
    com.example.et.module.user.dto.UpdateUserDetailsDto configDto = new com.example.et.module.user.dto.UpdateUserDetailsDto(
        com.example.et.module.user.AppUserConfig.LanguagePreference.EN,
        50000,
        com.example.et.module.user.AppUserConfig.Currency.INR,
        "UPI",
        true
    );
    OnboardUserDto request = new OnboardUserDto(configDto, 500.0f, List.of(accountDetailsResponse));

    when(accountService.addAccounts(eq(userId), any())).thenReturn(List.of(accountDetailsResponse));
    when(accountService.updateCashBalance(userId, 500.0f)).thenReturn(500.0f);
    when(appUserService.updateUserConfig(userId, configDto)).thenReturn(configDto);

    OnboardUserDto response = dashboardService.onboardUser(userId, request);

    assertNotNull(response);
    assertEquals(500.0f, response.cashBalance());
    assertEquals(1, response.accounts().size());
    assertEquals(configDto, response.userConfig());

    verify(accountService, times(1)).addAccounts(eq(userId), any());
    verify(accountService, times(1)).updateCashBalance(userId, 500.0f);
    verify(appUserService, times(1)).updateUserConfig(userId, configDto);
  }

  // ----------------------------------------------------------------------
  // getCategoryBreakdown
  // ----------------------------------------------------------------------
  @Test
  void getCategoryBreakdown_ShouldGroupAndSortExpensesByCategory() {
    LocalDate now = LocalDate.now();
    int year = now.getYear();
    int month = now.getMonthValue();

    TransactionDetailsResponse food1 = new TransactionDetailsResponse(
        UUID.randomUUID(), Transaction.TransactionType.EXPENSE, 100.0f, now, "Lunch", "Acc", "UPI", "Food"
    );
    TransactionDetailsResponse food2 = new TransactionDetailsResponse(
        UUID.randomUUID(), Transaction.TransactionType.EXPENSE, 200.0f, now, "Dinner", "Acc", "UPI", "Food"
    );
    TransactionDetailsResponse transport = new TransactionDetailsResponse(
        UUID.randomUUID(), Transaction.TransactionType.EXPENSE, 100.0f, now, "Cab", "Acc", "UPI", "Transport"
    );
    TransactionDetailsResponse income = new TransactionDetailsResponse(
        UUID.randomUUID(), Transaction.TransactionType.INCOME, 5000.0f, now, "Salary", "Acc", "UPI", "Salary"
    );

    PagedTransactionsResponse pagedTxns = new PagedTransactionsResponse(List.of(food1, food2, transport, income), 0, 10, 4, 1, true);

    when(transactionService.getAllTransactions(eq(userId), any(TransactionFilterParams.class), any(Pageable.class)))
        .thenReturn(pagedTxns);

    CategoryBreakdownResponse breakdown = dashboardService.getCategoryBreakdown(userId, year, month);

    assertNotNull(breakdown);
    assertEquals(2, breakdown.content().size());

    // Food should be 1st (total 300.0, 75%)
    CategoryBreakdown first = breakdown.content().get(0);
    assertEquals("Food", first.categoryName());
    assertEquals(300.0, first.totalAmount(), 0.001);
    assertEquals(75.0, first.percentage(), 0.001);
    assertEquals(2L, first.transactionCount());

    // Transport should be 2nd (total 100.0, 25%)
    CategoryBreakdown second = breakdown.content().get(1);
    assertEquals("Transport", second.categoryName());
    assertEquals(100.0, second.totalAmount(), 0.001);
    assertEquals(25.0, second.percentage(), 0.001);
    assertEquals(1L, second.transactionCount());
  }

  @Test
  void getCategoryBreakdown_ShouldHandleUncategorizedAndNullYearMonth() {
    TransactionDetailsResponse txn = new TransactionDetailsResponse(
        UUID.randomUUID(), Transaction.TransactionType.EXPENSE, 50.0f, LocalDate.now(), "Unknown", "Acc", "UPI", null
    );

    PagedTransactionsResponse pagedTxns = new PagedTransactionsResponse(List.of(txn), 0, 10, 1, 1, true);

    when(transactionService.getAllTransactions(eq(userId), any(TransactionFilterParams.class), any(Pageable.class)))
        .thenReturn(pagedTxns);

    CategoryBreakdownResponse breakdown = dashboardService.getCategoryBreakdown(userId, null, null);

    assertNotNull(breakdown);
    assertEquals(1, breakdown.content().size());
    assertEquals("Uncategorized", breakdown.content().get(0).categoryName());
    assertEquals(50.0, breakdown.content().get(0).totalAmount(), 0.001);
    assertEquals(100.0, breakdown.content().get(0).percentage(), 0.001);
  }

  @Test
  void getCategoryBreakdown_ShouldReturnEmptyList_WhenNoExpenses() {
    PagedTransactionsResponse emptyTxns = new PagedTransactionsResponse(Collections.emptyList(), 0, 10, 0, 0, true);

    when(transactionService.getAllTransactions(eq(userId), any(TransactionFilterParams.class), any(Pageable.class)))
        .thenReturn(emptyTxns);

    CategoryBreakdownResponse breakdown = dashboardService.getCategoryBreakdown(userId, 2026, 1);

    assertNotNull(breakdown);
    assertTrue(breakdown.content().isEmpty());
  }

  // ----------------------------------------------------------------------
  // getSummary
  // ----------------------------------------------------------------------
  @Test
  void getSummary_ShouldCalculateNetWorthIncomeExpenseSavingsAndBurnRate() {
    AccountDetailsResponse savings = new AccountDetailsResponse(UUID.randomUUID(), "1111", 1000.0f, Account.AccountType.SAVINGS, true, true, null, true);
    AccountDetailsResponse credit = new AccountDetailsResponse(UUID.randomUUID(), "2222", 200.0f, Account.AccountType.CREDIT, true, true, null, true);

    when(accountService.getUserAccounts(userId)).thenReturn(List.of(savings, credit));

    LocalDate now = LocalDate.now();
    TransactionDetailsResponse expense = new TransactionDetailsResponse(
        UUID.randomUUID(), Transaction.TransactionType.EXPENSE, 1500.0f, now, "Rent", "Acc", "UPI", "Housing"
    );
    TransactionDetailsResponse income = new TransactionDetailsResponse(
        UUID.randomUUID(), Transaction.TransactionType.INCOME, 5000.0f, now, "Salary", "Acc", "UPI", "Salary"
    );

    PagedTransactionsResponse pagedTxns = new PagedTransactionsResponse(List.of(expense, income), 0, 10, 2, 1, true);
    when(transactionService.getAllTransactions(eq(userId), any(TransactionFilterParams.class), any(Pageable.class)))
        .thenReturn(pagedTxns);

    UserSummaryDto summary = dashboardService.getSummary(userId);

    assertNotNull(summary);
    // Net worth = 1000 - 200 = 800
    assertEquals(800.0, summary.netWorth(), 0.001);
    assertEquals(5000.0, summary.totalIncome(), 0.001);
    assertEquals(1500.0, summary.totalExpense(), 0.001);
    assertEquals(3500.0, summary.netSavings(), 0.001);
    assertEquals(1500.0 / now.getDayOfMonth(), summary.dailyBurnRate(), 0.001);
  }

  @Test
  void getSummary_ShouldReturnEmptySummary_WhenExceptionOccurs() {
    when(accountService.getUserAccounts(userId)).thenThrow(new RuntimeException("DB down"));

    UserSummaryDto summary = dashboardService.getSummary(userId);

    assertNotNull(summary);
    assertEquals(0.0, summary.netWorth());
    assertEquals(0.0, summary.totalIncome());
    assertEquals(0.0, summary.totalExpense());
    assertEquals(0.0, summary.netSavings());
    assertEquals(0.0, summary.dailyBurnRate());
  }

  // ----------------------------------------------------------------------
  // getMonthlyTrend
  // ----------------------------------------------------------------------
  @Test
  void getMonthlyTrend_ShouldCalculateTrendsAcrossMonths() {
    LocalDate now = LocalDate.now();
    TransactionDetailsResponse expense = new TransactionDetailsResponse(
        UUID.randomUUID(), Transaction.TransactionType.EXPENSE, 500.0f, now, "Groceries", "Acc", "UPI", "Food"
    );
    TransactionDetailsResponse income = new TransactionDetailsResponse(
        UUID.randomUUID(), Transaction.TransactionType.INCOME, 2000.0f, now, "Paycheck", "Acc", "UPI", "Salary"
    );

    PagedTransactionsResponse pagedTxns = new PagedTransactionsResponse(List.of(expense, income), 0, 10, 2, 1, true);
    when(transactionService.getAllTransactions(eq(userId), any(TransactionFilterParams.class), any(Pageable.class)))
        .thenReturn(pagedTxns);

    List<MonthlyTrendDto> trends = dashboardService.getMonthlyTrend(userId, 3);

    assertNotNull(trends);
    assertEquals(3, trends.size());

    // Current month is the last one in the returned list
    MonthlyTrendDto currentMonthTrend = trends.get(2);
    assertEquals(2000.0, currentMonthTrend.totalIncome(), 0.001);
    assertEquals(500.0, currentMonthTrend.totalExpense(), 0.001);
    assertEquals(1500.0, currentMonthTrend.netSavings(), 0.001);
  }

  @Test
  void getMonthlyTrend_Default_ShouldDefaultToSixMonths() {
    PagedTransactionsResponse emptyTxns = new PagedTransactionsResponse(Collections.emptyList(), 0, 10, 0, 0, true);
    when(transactionService.getAllTransactions(eq(userId), any(TransactionFilterParams.class), any(Pageable.class)))
        .thenReturn(emptyTxns);

    List<MonthlyTrendDto> trends = dashboardService.getMonthlyTrend(userId);

    assertNotNull(trends);
    assertEquals(6, trends.size());
  }
}
