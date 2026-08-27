package com.example.et.service.dataseeder;

import com.example.et.controller.dto.*;
import com.example.et.model.core.*;
import com.example.et.repo.BankRepo;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.repo.SystemCategoryRepo;
import com.example.et.service.account.AccountService;
import com.example.et.service.appuser.AppUserService;
import com.example.et.service.auth.AuthService;
import com.example.et.service.card.CardService;
import com.example.et.service.dashboard.DashboardService;
import com.example.et.service.transaction.TransactionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeederService {
  private final AuthService authService;
  private final AppUserService appUserService;
  private final DashboardService dashboardService;
  private final AccountService accountService;
  private final CardService cardService;
  private final TransactionsService transactionsService;
  private final BankRepo bankRepo;
  private final PaymentModeRepo paymentModeRepo;
  private final SystemCategoryRepo systemCategoryRepo;
  private final Faker faker = new Faker();

  @Transactional
  public int seedUsers(int count) {
    for (int i = 0; i < count; i++) {
      final var request = new UserRegistrationRequest(
          faker.name().fullName(),
          faker.internet().emailAddress(),
          "Password@123"
      );
      authService.registerUser(request);
    }
    return count;
  }

  public int seedFullData(int usersCount, int monthsOfTransactions) {
    int seededUsers = 0;
    for (int i = 0; i < usersCount; i++) {
      try {
        final var name = faker.name().fullName();
        final var email = faker.internet().emailAddress();
        seedUserFullJourney(name, email, monthsOfTransactions);
        seededUsers++;
      } catch (Exception e) {
        log.error("Failed seeding full data for user: ", e);
      }
    }
    return seededUsers;
  }

  public String seedUserFullJourney(String name, String email, int monthsOfTransactions) {
    // 1. Register User (AuthController method -> AuthService)
    final var regRequest = new UserRegistrationRequest(name, email, "test@1234");
    authService.registerUser(regRequest);

    final var appUser = appUserService.getUserByEmail(email);
    final var userId = appUser.getId().toString();

    // 2. Prepare Banks, Payment Modes, Categories
    final var banks = bankRepo.findAll();
    final var paymentModes = paymentModeRepo.findAll();
    final var categories = systemCategoryRepo.findAll();

    final var primaryBank = !banks.isEmpty() ? banks.get(faker.random().nextInt(banks.size())) : null;
    final var secondaryBank = banks.size() > 1 ? banks.get((banks.indexOf(primaryBank) + 1) % banks.size()) : primaryBank;

    final var upiPaymentMode = paymentModes.stream()
        .filter(pm -> pm.getName().equalsIgnoreCase("UPI / NetBanking"))
        .findFirst()
        .orElse(!paymentModes.isEmpty() ? paymentModes.get(0) : null);

    final var cashPaymentMode = paymentModes.stream()
        .filter(pm -> pm.getName().equalsIgnoreCase("Cash"))
        .findFirst()
        .orElse(upiPaymentMode);

    final var debitCardPaymentMode = paymentModes.stream()
        .filter(pm -> pm.getName().equalsIgnoreCase("Debit Card"))
        .findFirst()
        .orElse(upiPaymentMode);

    final var creditCardPaymentMode = paymentModes.stream()
        .filter(pm -> pm.getName().equalsIgnoreCase("Credit Card"))
        .findFirst()
        .orElse(upiPaymentMode);

    // 3. Onboard User (DashboardController method -> DashboardService)
    final var savingsAccountDto = new AccountDto(
        null,
        String.valueOf(faker.number().numberBetween(1000, 9999)),
        (float) faker.number().randomDouble(2, 50000, 150000),
        Account.AccountType.SAVINGS,
        primaryBank,
        true,
        true
    );

    final var userConfig = new UpdateUserDetailsDto(
        LanguagePreference.EN,
        faker.number().numberBetween(30000, 80000),
        AppUserConfig.Currency.INR,
        upiPaymentMode != null ? upiPaymentMode.getName() : "Cash",
        true
    );

    final var initialCashBalance = (float) faker.number().randomDouble(2, 3000, 10000);
    dashboardService.onboardUser(userId, new OnboardUserDto(userConfig, List.of(savingsAccountDto), initialCashBalance));

    // 4. Retrieve created accounts and Add Cards (CardController method -> CardService)
    final var userAccounts = accountService.getUserAccounts(userId);
    final var savingsAccount = userAccounts.stream()
        .filter(acc -> acc.accountType() == Account.AccountType.SAVINGS)
        .findFirst()
        .orElse(null);

    if (savingsAccount != null && primaryBank != null) {
      final var debitCardDto = new CardDto(
          null,
          CardType.DEBIT_CARD,
          String.valueOf(faker.number().numberBetween(1000, 9999)),
          savingsAccount.id(),
          null,
          primaryBank
      );

      final var creditCardDto = new CardDto(
          null,
          CardType.CREDIT_CARD,
          String.valueOf(faker.number().numberBetween(1000, 9999)),
          null,
          (float) faker.number().randomDouble(2, 75000, 200000),
          secondaryBank != null ? secondaryBank : primaryBank
      );

      cardService.addCards(userId, new UserCards(List.of(debitCardDto, creditCardDto)));
    }

    // 5. Retrieve all user cards & accounts for transaction seeding
    final var debitCard = cardService.getUserCards(userId, CardType.DEBIT_CARD).stream().findFirst().orElse(null);
    final var creditCard = cardService.getUserCards(userId, CardType.CREDIT_CARD).stream().findFirst().orElse(null);
    final var cashAccountDto = accountService.getUserCashAccountDetails(userId);

    // 6. Generate Realistic Multi-Month Transactions (TransactionsController method -> TransactionsService)
    seedTransactionsForUser(
        userId,
        savingsAccount != null ? savingsAccount.id() : null,
        cashAccountDto != null ? cashAccountDto.id() : null,
        debitCard != null ? debitCard.id() : null,
        creditCard != null ? creditCard.id() : null,
        upiPaymentMode,
        cashPaymentMode,
        debitCardPaymentMode,
        creditCardPaymentMode,
        categories,
        monthsOfTransactions
    );

    return userId;
  }

  private void seedTransactionsForUser(
      String userId,
      UUID savingsAccountId,
      UUID cashAccountId,
      UUID debitCardId,
      UUID creditCardId,
      PaymentMode upiPaymentMode,
      PaymentMode cashPaymentMode,
      PaymentMode debitCardPaymentMode,
      PaymentMode creditCardPaymentMode,
      List<SystemCategory> categories,
      int months
  ) {
    if (savingsAccountId == null) return;

    final var now = LocalDate.now();
    final var startMonth = now.minusMonths(Math.max(1, months));

    for (int m = 0; m <= months; m++) {
      final var currentMonthDate = startMonth.plusMonths(m);
      final int year = currentMonthDate.getYear();
      final int month = currentMonthDate.getMonthValue();
      final int daysInMonth = (year == now.getYear() && month == now.getMonthValue())
          ? now.getDayOfMonth()
          : currentMonthDate.lengthOfMonth();

      // --- Income Transactions (Always credit salary at start of month to maintain healthy balance) ---
      // 1. Monthly Salary (Day 1)
      final var salaryDate = LocalDate.of(year, month, Math.min(1, daysInMonth));
      final var salaryCategory = findCategory(categories, "Investments", "Miscellaneous");
      transactionsService.createTransaction(userId, new TransactionRequestDto(
          null,
          Transaction.TransactionType.INCOME,
          (float) faker.number().randomDouble(2, 80000, 140000),
          salaryDate,
          "Monthly Salary Credit",
          savingsAccountId,
          null,
          null,
          upiPaymentMode != null ? upiPaymentMode.getId() : null,
          salaryCategory != null ? salaryCategory.getId() : null,
          null
      ));

      // 2. ATM Cash Withdrawal (Day 2 - ensure cash account has sufficient balance)
      if (cashAccountId != null && daysInMonth >= 2) {
        final var transferDate = LocalDate.of(year, month, 2);
        final var savingsAcc = accountService.getUserAccount(userId, savingsAccountId);
        if (savingsAcc.getBalance() != null && savingsAcc.getBalance() >= 10000.0f) {
          transactionsService.createTransaction(userId, new TransactionRequestDto(
              null,
              Transaction.TransactionType.TRANSFER,
              5000.0f,
              transferDate,
              "ATM Cash Withdrawal",
              savingsAccountId,
              null,
              cashAccountId,
              cashPaymentMode != null ? cashPaymentMode.getId() : null,
              null,
              null
          ));
        }
      }

      // 3. Rent / EMI Expense (Day 3)
      if (daysInMonth >= 3) {
        final var rentCategory = findCategory(categories, "Rent/EMI", "Utilities");
        safeCreateExpense(
            userId,
            savingsAccountId,
            cashAccountId,
            debitCardId,
            creditCardId,
            upiPaymentMode,
            cashPaymentMode,
            debitCardPaymentMode,
            creditCardPaymentMode,
            rentCategory,
            new ExpenseDetail("Monthly House Rent", (float) faker.number().randomDouble(2, 15000, 30000)),
            LocalDate.of(year, month, 3)
        );
      }

      // 4. Utilities Expense (Day 10)
      if (daysInMonth >= 10) {
        final var utilCategory = findCategory(categories, "Utilities (Electricity/Water)", "Utilities");
        safeCreateExpense(
            userId,
            savingsAccountId,
            cashAccountId,
            debitCardId,
            creditCardId,
            upiPaymentMode,
            cashPaymentMode,
            debitCardPaymentMode,
            creditCardPaymentMode,
            utilCategory,
            new ExpenseDetail("Electricity & Water Bill", (float) faker.number().randomDouble(2, 1200, 4500)),
            LocalDate.of(year, month, 10)
        );
      }

      // 5. Mid-month Freelance / Bonus Income (Day 15)
      if (daysInMonth >= 15) {
        final var bonusDate = LocalDate.of(year, month, 15);
        transactionsService.createTransaction(userId, new TransactionRequestDto(
            null,
            Transaction.TransactionType.INCOME,
            (float) faker.number().randomDouble(2, 15000, 35000),
            bonusDate,
            "Freelance Project Payout",
            savingsAccountId,
            null,
            null,
            upiPaymentMode != null ? upiPaymentMode.getId() : null,
            salaryCategory != null ? salaryCategory.getId() : null,
            null
        ));
      }

      // 6. Multiple variable expenses throughout the month (safely managed with balance check)
      final int expenseCount = faker.number().numberBetween(8, 16);
      for (int k = 0; k < expenseCount; k++) {
        final int day = faker.number().numberBetween(1, daysInMonth + 1);
        final var txnDate = LocalDate.of(year, month, Math.min(day, daysInMonth));
        final var category = !categories.isEmpty() ? categories.get(faker.random().nextInt(categories.size())) : null;
        final var categoryName = category != null ? category.getName() : "Miscellaneous";
        final var expenseInfo = generateExpenseInfo(categoryName);

        safeCreateExpense(
            userId,
            savingsAccountId,
            cashAccountId,
            debitCardId,
            creditCardId,
            upiPaymentMode,
            cashPaymentMode,
            debitCardPaymentMode,
            creditCardPaymentMode,
            category,
            expenseInfo,
            txnDate
        );
      }
    }
  }

  private void safeCreateExpense(
      String userId,
      UUID savingsAccountId,
      UUID cashAccountId,
      UUID debitCardId,
      UUID creditCardId,
      PaymentMode upiPaymentMode,
      PaymentMode cashPaymentMode,
      PaymentMode debitCardPaymentMode,
      PaymentMode creditCardPaymentMode,
      SystemCategory category,
      ExpenseDetail expenseInfo,
      LocalDate txnDate
  ) {
    UUID accountId = savingsAccountId;
    UUID cardId = null;
    UUID paymentModeId = upiPaymentMode != null ? upiPaymentMode.getId() : null;

    int choice = faker.number().numberBetween(0, 4);
    if (choice == 0 && creditCardId != null && creditCardPaymentMode != null) {
      accountId = null;
      cardId = creditCardId;
      paymentModeId = creditCardPaymentMode.getId();
    } else if (choice == 1 && debitCardId != null && debitCardPaymentMode != null) {
      accountId = null;
      cardId = debitCardId;
      paymentModeId = debitCardPaymentMode.getId();
    } else if (choice == 2 && cashAccountId != null && cashPaymentMode != null) {
      accountId = cashAccountId;
      paymentModeId = cashPaymentMode.getId();
    }

    // Resolve target account to check live balance
    Account targetAccount = getTargetAccount(userId, accountId, cardId);
    float amount = expenseInfo.amount();

    if (targetAccount != null) {
      float currentBalance = targetAccount.getBalance() != null ? targetAccount.getBalance() : 0.0f;

      // If cash account has insufficient balance
      if (targetAccount.getAccountType() == Account.AccountType.CASH && currentBalance - amount < 200.0f) {
        // Try to withdraw from savings to cash first
        final var savingsAccount = accountService.getUserAccount(userId, savingsAccountId);
        if (savingsAccount.getBalance() != null && savingsAccount.getBalance() >= 6000.0f) {
          transactionsService.createTransaction(userId, new TransactionRequestDto(
              null,
              Transaction.TransactionType.TRANSFER,
              5000.0f,
              txnDate,
              "ATM Cash Withdrawal",
              savingsAccountId,
              null,
              cashAccountId,
              cashPaymentMode != null ? cashPaymentMode.getId() : null,
              null,
              null
          ));
          targetAccount = accountService.getUserAccount(userId, cashAccountId);
          currentBalance = targetAccount.getBalance() != null ? targetAccount.getBalance() : 0.0f;
        } else {
          // Fallback to UPI from savings
          accountId = savingsAccountId;
          cardId = null;
          paymentModeId = upiPaymentMode != null ? upiPaymentMode.getId() : null;
          targetAccount = savingsAccount;
          currentBalance = targetAccount.getBalance() != null ? targetAccount.getBalance() : 0.0f;
        }
      }

      // If credit card account has insufficient limit/balance
      if (targetAccount.getAccountType() == Account.AccountType.CREDIT && currentBalance - amount < 500.0f) {
        // Fallback to UPI from savings
        accountId = savingsAccountId;
        cardId = null;
        paymentModeId = upiPaymentMode != null ? upiPaymentMode.getId() : null;
        targetAccount = accountService.getUserAccount(userId, savingsAccountId);
        currentBalance = targetAccount.getBalance() != null ? targetAccount.getBalance() : 0.0f;
      }

      // If savings account balance is getting low, inject an income top-up before the expense
      if (currentBalance - amount < 1000.0f) {
        transactionsService.createTransaction(userId, new TransactionRequestDto(
            null,
            Transaction.TransactionType.INCOME,
            60000.0f,
            txnDate,
            "Consulting Payout / Returns",
            savingsAccountId,
            null,
            null,
            upiPaymentMode != null ? upiPaymentMode.getId() : null,
            null,
            null
        ));
      }
    }

    transactionsService.createTransaction(userId, new TransactionRequestDto(
        null,
        Transaction.TransactionType.EXPENSE,
        amount,
        txnDate,
        expenseInfo.description(),
        accountId,
        cardId,
        null,
        paymentModeId,
        category != null ? category.getId() : null,
        null
    ));
  }

  private Account getTargetAccount(String userId, UUID accountId, UUID cardId) {
    if (cardId != null) {
      final var card = cardService.getUserCard(userId, cardId);
      return card.getAccount();
    }
    if (accountId != null) {
      return accountService.getUserAccount(userId, accountId);
    }
    return null;
  }

  private SystemCategory findCategory(List<SystemCategory> categories, String... names) {
    for (String name : names) {
      for (SystemCategory category : categories) {
        if (category.getName().toLowerCase().contains(name.toLowerCase())) {
          return category;
        }
      }
    }
    return categories.isEmpty() ? null : categories.get(0);
  }

  private record ExpenseDetail(String description, Float amount) {}

  private ExpenseDetail generateExpenseInfo(String categoryName) {
    final String cat = categoryName.toLowerCase();
    if (cat.contains("grocer")) {
      return new ExpenseDetail(
          faker.options().option("Supermarket Grocery", "Fresh Vegetables & Fruits", "Organic Store", "Hypermarket Run"),
          (float) faker.number().randomDouble(2, 400, 3500)
      );
    } else if (cat.contains("dining")) {
      return new ExpenseDetail(
          faker.options().option("Dinner with Friends", "Cafe Coffee & Pastry", "Weekend Brunch", "Takeout Order", "Italian Restaurant"),
          (float) faker.number().randomDouble(2, 250, 2200)
      );
    } else if (cat.contains("fuel") || cat.contains("transport")) {
      return new ExpenseDetail(
          faker.options().option("Petrol Refill", "Uber Ride", "Metro Card Recharge", "Auto Rickshaw Fare"),
          (float) faker.number().randomDouble(2, 150, 1800)
      );
    } else if (cat.contains("shopping")) {
      return new ExpenseDetail(
          faker.options().option("Apparel & Clothing", "Amazon Online Purchase", "Electronics Accessories", "Home Decor Item"),
          (float) faker.number().randomDouble(2, 800, 6000)
      );
    } else if (cat.contains("entertainment")) {
      return new ExpenseDetail(
          faker.options().option("Movie Tickets & Popcorn", "Netflix Subscription", "Spotify Premium", "Concert Pass"),
          (float) faker.number().randomDouble(2, 199, 1500)
      );
    } else if (cat.contains("health") || cat.contains("medical")) {
      return new ExpenseDetail(
          faker.options().option("Pharmacy Medicines", "Doctor Consultation", "Dental Checkup", "Diagnostic Lab Test"),
          (float) faker.number().randomDouble(2, 300, 3000)
      );
    } else if (cat.contains("invest")) {
      return new ExpenseDetail(
          faker.options().option("Mutual Fund SIP", "Stock Purchase", "Gold Accumulation", "Fixed Deposit Deposit"),
          (float) faker.number().randomDouble(2, 2000, 15000)
      );
    } else {
      return new ExpenseDetail(
          faker.options().option("Daily Needs Expense", "Local Store Purchase", "Stationery & Supplies", "Courier Delivery"),
          (float) faker.number().randomDouble(2, 100, 1200)
      );
    }
  }
}