package com.example.et.module.dataseeder.internal;

import com.example.et.module.account.Account;
import com.example.et.module.account.AccountService;
import com.example.et.module.account.dto.AccountDto;
import com.example.et.module.auth.AuthService;
import com.example.et.module.auth.dto.RegisterUserRequest;
import com.example.et.module.card.Card;
import com.example.et.module.card.CardService;
import com.example.et.module.card.dto.AddCardsRequest;
import com.example.et.module.card.dto.CardResponse;
import com.example.et.module.dashboard.DashboardService;
import com.example.et.module.dashboard.dto.OnboardUserDto;
import com.example.et.module.dataseeder.DataSeederService;
import com.example.et.module.dataseeder.ExpenseCatalog;
import com.example.et.module.dataseeder.SeedReferenceData;
import com.example.et.module.dataseeder.UserSeedState;
import com.example.et.module.reference.bank.BankMapper;
import com.example.et.module.reference.bank.internal.BankRepo;
import com.example.et.module.reference.category.SystemCategory;
import com.example.et.module.reference.category.internal.SysCategoryRepo;
import com.example.et.module.reference.paymentmode.PaymentMode;
import com.example.et.module.reference.paymentmode.internal.PaymentModeRepo;
import com.example.et.module.transaction.Transaction;
import com.example.et.module.transaction.TransactionService;
import com.example.et.module.transaction.dto.TransactionRequestDto;
import com.example.et.module.user.AppUserConfig;
import com.example.et.module.user.AppUserService;
import com.example.et.module.user.dto.UpdateUserDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeederServiceImpl implements DataSeederService {

  private static final String DEFAULT_USER_PASSWORD = "Password@123";
  private static final String DEFAULT_JOURNEY_PASSWORD = "test@1234";

  private final Faker faker = new Faker();
  private final ExpenseCatalog expenseCatalog = new ExpenseCatalog(faker);

  private final AuthService authService;
  private final AppUserService appUserService;
  private final DashboardService dashboardService;
  private final AccountService accountService;
  private final CardService cardService;
  private final TransactionService transactionsService;
  private final BankRepo bankRepo;
  private final PaymentModeRepo paymentModeRepo;
  private final SysCategoryRepo systemCategoryRepo;
  private final BankMapper bankMapper;

  @Override
  public int seedUsers(int count) {
    log.info("========================================================");
    log.info("[Users Seeder] Starting bulk creation of {} users", count);
    for (int i = 0; i < count; i++) {
      final String name = faker.name().fullName();
      final String email = faker.internet().emailAddress();
      log.info("[Users Seeder] [{}/{}] Registering user '{}' ({})", i + 1, count, name, email);

      final var request = new RegisterUserRequest(name, email, DEFAULT_USER_PASSWORD);
      authService.register(request);
      log.info("[Users Seeder] [{}/{}] User registered successfully: {}", i + 1, count, email);

      if (i < count - 1) {
        delay("registering next user");
      }
    }
    log.info("[Users Seeder] Completed bulk creation of {} users", count);
    log.info("========================================================");
    return count;
  }

  @Override
  public int seedData(int noOfUsers, int monthsOfTransactions) {
    final long startTime = System.currentTimeMillis();
    log.info("========================================================");
    log.info("[Data Seeder] Initiating full data seeding: {} user(s), {} month(s) each", noOfUsers, monthsOfTransactions);

    // Cache static reference data once for the entire batch to eliminate repeated cloud DB queries
    final SeedReferenceData refData = loadReferenceData();
    log.info("[Data Seeder] Reference data loaded: {} banks, {} payment modes, {} categories",
        refData.banks().size(),
        (refData.upiPaymentMode() != null ? 1 : 0) + (refData.cashPaymentMode() != null ? 1 : 0)
            + (refData.debitCardPaymentMode() != null ? 1 : 0) + (refData.creditCardPaymentMode() != null ? 1 : 0),
        refData.categories().size()
    );

    int seededUsers = 0;
    for (int i = 0; i < noOfUsers; i++) {
      final String name = faker.name().fullName();
      final String email = faker.internet().emailAddress();
      log.info("--------------------------------------------------------");
      log.info("[Data Seeder] Starting journey for user [{}/{}]: {} ({})", i + 1, noOfUsers, name, email);

      try {
        seedUserFullJourney(name, email, monthsOfTransactions, refData, i + 1, noOfUsers);
        seededUsers++;
        log.info("[Data Seeder] Finished journey for user [{}/{}]: {}", i + 1, noOfUsers, email);
      } catch (Exception e) {
        log.error("[Data Seeder] Failed seeding data for user [{}/{}]: {} ({})", i + 1, noOfUsers, email, e.getMessage(), e);
      }

      if (i < noOfUsers - 1) {
        delay(String.format("initiating journey for user %d/%d", i + 2, noOfUsers));
      }
    }

    final long duration = System.currentTimeMillis() - startTime;
    log.info("========================================================");
    log.info("[Data Seeder] Finished full data seeding: {}/{} users seeded successfully in {} ms ({} min)",
        seededUsers, noOfUsers, duration, String.format("%.2f", duration / 60000.0));
    log.info("========================================================");
    return seededUsers;
  }

  public void seedUserFullJourney(String name, String email, int monthsOfTransactions) {
    seedUserFullJourney(name, email, monthsOfTransactions, loadReferenceData(), 1, 1);
  }

  public void seedUserFullJourney(
      String name,
      String email,
      int monthsOfTransactions,
      SeedReferenceData refData,
      int userIndex,
      int totalUsers
  ) {
    final String userTag = String.format("[User %d/%d - %s]", userIndex, totalUsers, name);

    // --- Step 1: User Registration ---
    log.info("{} [Step 1/4] Registering user account with email: {}", userTag, email);
    authService.register(new RegisterUserRequest(name, email, DEFAULT_JOURNEY_PASSWORD));
    final var appUser = appUserService.getUserByEmail(email);
    final var userId = appUser.getId().toString();
    log.info("{} [Step 1/4] Registered successfully. Resolved User ID: {}", userTag, userId);

    delay("Step 2/4: User Onboarding & Account Setup");

    // --- Step 2: User Onboarding & Accounts ---
    final var primaryBank = refData.getRandomBank(faker);
    final var secondaryBank = refData.getSecondaryBank(primaryBank);

    final float initialSavingsBalance = (float) faker.number().randomDouble(2, 50000, 150000);
    final float initialCashBalance = (float) faker.number().randomDouble(2, 3000, 10000);

    log.info("{} [Step 2/4] Onboarding user: creating Savings account (Bank: {}, Balance: ₹{}) & Cash account (Balance: ₹{})...",
        userTag,
        primaryBank != null ? primaryBank.name() : "N/A",
        formatAmount(initialSavingsBalance),
        formatAmount(initialCashBalance)
    );

    final var savingsAccountDto = new AccountDto(
        null,
        String.valueOf(faker.number().numberBetween(1000, 9999)),
        initialSavingsBalance,
        Account.AccountType.SAVINGS,
        true,
        true,
        primaryBank,
        true
    );

    final var userConfig = new UpdateUserDetailsDto(
        AppUserConfig.LanguagePreference.EN,
        faker.number().numberBetween(30000, 80000),
        AppUserConfig.Currency.INR,
        refData.upiPaymentMode() != null ? refData.upiPaymentMode().getName() : "Cash",
        true
    );

    final var onboardResult = dashboardService.onboardUser(
        userId,
        new OnboardUserDto(userConfig, initialCashBalance, List.of(savingsAccountDto))
    );

    final var savingsAccount = onboardResult.accounts() != null
        ? onboardResult.accounts().stream()
        .filter(acc -> acc.accountType() == Account.AccountType.SAVINGS)
        .findFirst()
        .orElse(null)
        : null;

    if (savingsAccount == null || primaryBank == null) {
      log.warn("{} [Step 2/4] Skipping card and transaction seeding due to missing savings account or bank", userTag);
      return;
    }

    final var cashAccountDto = accountService.getUserCashAccountDetails(userId);
    final UUID cashAccountId = cashAccountDto != null ? cashAccountDto.id() : null;

    log.info("{} [Step 2/4] Onboarding complete. Savings Account ID: {}, Cash Account ID: {}",
        userTag, savingsAccount.id(), cashAccountId);

    delay("Step 3/4: Card Provisioning");

    // --- Step 3: Card Issuance ---
    final float initialCreditLimit = (float) faker.number().randomDouble(2, 75000, 200000);

    log.info("{} [Step 3/4] Issuing Debit Card (linked to Savings) & Credit Card (Bank: {}, Limit: ₹{})...",
        userTag,
        secondaryBank != null ? secondaryBank.name() : primaryBank.name(),
        formatAmount(initialCreditLimit)
    );

    final var debitCardDto = new CardResponse(
        null,
        Card.CardType.DEBIT_CARD,
        String.valueOf(faker.number().numberBetween(1000, 9999)),
        savingsAccount.id().toString(),
        null,
        bankMapper.toEntity(primaryBank)
    );

    final var creditCardDto = new CardResponse(
        null,
        Card.CardType.CREDIT_CARD,
        String.valueOf(faker.number().numberBetween(1000, 9999)),
        null,
        initialCreditLimit,
        bankMapper.toEntity(secondaryBank != null ? secondaryBank : primaryBank)
    );

    final var createdCards = cardService.addCards(userId, new AddCardsRequest(List.of(debitCardDto, creditCardDto)));

    final UUID debitCardId = createdCards.cards().stream()
        .filter(c -> c.cardType() == Card.CardType.DEBIT_CARD)
        .map(CardResponse::id)
        .findFirst()
        .orElse(null);

    final UUID creditCardId = createdCards.cards().stream()
        .filter(c -> c.cardType() == Card.CardType.CREDIT_CARD)
        .map(CardResponse::id)
        .findFirst()
        .orElse(null);

    log.info("{} [Step 3/4] Cards issued. Debit Card ID: {}, Credit Card ID: {}", userTag, debitCardId, creditCardId);

    delay("Step 4/4: Multi-Month Transaction Seeding");

    // --- Step 4: Transaction History Seeding ---
    final var userState = new UserSeedState(
        userId,
        savingsAccount.id(),
        initialSavingsBalance,
        cashAccountId,
        initialCashBalance,
        debitCardId,
        creditCardId,
        initialCreditLimit
    );

    log.info("{} [Step 4/4] Starting transaction simulation across {} month(s)...", userTag, monthsOfTransactions);
    seedTransactionsForUser(userState, refData, monthsOfTransactions, userTag);
    log.info("{} [Step 4/4] All transactions completed. Final Balances -> Savings: ₹{}, Cash: ₹{}, Credit Avail: ₹{}",
        userTag,
        formatAmount(userState.getSavingsBalance()),
        formatAmount(userState.getCashBalance()),
        formatAmount(userState.getCreditBalance())
    );
  }

  private void seedTransactionsForUser(
      UserSeedState state,
      SeedReferenceData refData,
      int months,
      String userTag
  ) {
    if (state.getSavingsAccountId() == null) return;

    final var now = LocalDate.now();
    final var startMonth = now.minusMonths(Math.max(1, months));

    for (int m = 0; m <= months; m++) {
      final var currentMonthDate = startMonth.plusMonths(m);
      final int year = currentMonthDate.getYear();
      final int month = currentMonthDate.getMonthValue();
      final int daysInMonth = (year == now.getYear() && month == now.getMonthValue())
          ? now.getDayOfMonth()
          : currentMonthDate.lengthOfMonth();

      log.info("{} >>> Month [{}/{}] ({}-{}) [Days: {}] <<<",
          userTag, m + 1, months + 1, year, String.format("%02d", month), daysInMonth);

      // --- 1. Monthly Salary Credit (Day 1) ---
      final var salaryDate = LocalDate.of(year, month, Math.min(1, daysInMonth));
      final var salaryCategory = refData.findCategoryByKeywords("Investments", "Miscellaneous");
      final float salaryAmount = (float) faker.number().randomDouble(2, 80000, 140000);

      log.info("{} [Month {}/{}] [Income] Crediting Monthly Salary of ₹{} on {}...",
          userTag, m + 1, months + 1, formatAmount(salaryAmount), salaryDate);

      transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
          null,
          Transaction.TransactionType.INCOME,
          salaryAmount,
          salaryDate,
          "Monthly Salary Credit",
          state.getSavingsAccountId().toString(),
          null,
          null,
          refData.upiPaymentMode() != null ? String.valueOf(refData.upiPaymentMode().getId()) : null,
          salaryCategory != null ? String.valueOf(salaryCategory.getId()) : null,
          null
      ));
      state.creditSavings(salaryAmount);
      log.info("{} [Month {}/{}] [Income] Salary credited. Live Savings Balance: ₹{}",
          userTag, m + 1, months + 1, formatAmount(state.getSavingsBalance()));

      delay("next operation: ATM cash withdrawal");

      // --- 2. ATM Cash Withdrawal (Day 2) ---
      if (state.getCashAccountId() != null && daysInMonth >= 2 && state.getSavingsBalance() >= 10000.0f) {
        final var transferDate = LocalDate.of(year, month, 2);
        final float withdrawalAmount = 5000.0f;

        log.info("{} [Month {}/{}] [Transfer] Executing ATM Cash Withdrawal of ₹{} on {}...",
            userTag, m + 1, months + 1, formatAmount(withdrawalAmount), transferDate);

        transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
            null,
            Transaction.TransactionType.TRANSFER,
            withdrawalAmount,
            transferDate,
            "ATM Cash Withdrawal",
            state.getSavingsAccountId().toString(),
            null,
            state.getCashAccountId().toString(),
            refData.cashPaymentMode() != null ? String.valueOf(refData.cashPaymentMode().getId()) : null,
            null,
            null
        ));
        state.transferSavingsToCash(withdrawalAmount);
        log.info("{} [Month {}/{}] [Transfer] ATM withdrawal complete. Savings: ₹{}, Cash: ₹{}",
            userTag, m + 1, months + 1, formatAmount(state.getSavingsBalance()), formatAmount(state.getCashBalance()));

        delay("next operation: Rent / EMI fixed expense");
      }

      // --- 3. Rent / EMI Fixed Expense (Day 3) ---
      if (daysInMonth >= 3) {
        final var rentDate = LocalDate.of(year, month, 3);
        final var rentCategory = refData.findCategoryByKeywords("Rent/EMI", "Utilities");
        final float rentAmount = (float) faker.number().randomDouble(2, 15000, 30000);
        final var rentDetail = new ExpenseCatalog.ExpenseDetail("Monthly House Rent", rentAmount);

        log.info("{} [Month {}/{}] [Fixed Expense] Paying Rent/EMI of ₹{} on {}...",
            userTag, m + 1, months + 1, formatAmount(rentAmount), rentDate);

        safeCreateExpense(state, refData, rentCategory, rentDetail, rentDate, userTag);

        delay("next operation: Utilities bill");
      }

      // --- 4. Utilities Fixed Expense (Day 10) ---
      if (daysInMonth >= 10) {
        final var utilDate = LocalDate.of(year, month, 10);
        final var utilCategory = refData.findCategoryByKeywords("Utilities (Electricity/Water)", "Utilities");
        final float utilAmount = (float) faker.number().randomDouble(2, 1200, 4500);
        final var utilDetail = new ExpenseCatalog.ExpenseDetail("Electricity & Water Bill", utilAmount);

        log.info("{} [Month {}/{}] [Fixed Expense] Paying Utilities bill of ₹{} on {}...",
            userTag, m + 1, months + 1, formatAmount(utilAmount), utilDate);

        safeCreateExpense(state, refData, utilCategory, utilDetail, utilDate, userTag);

        delay("next operation: mid-month freelance bonus");
      }

      // --- 5. Mid-month Freelance / Bonus Income (Day 15) ---
      if (daysInMonth >= 15) {
        final var bonusDate = LocalDate.of(year, month, 15);
        final float bonusAmount = (float) faker.number().randomDouble(2, 15000, 35000);

        log.info("{} [Month {}/{}] [Income] Crediting Freelance / Bonus payout of ₹{} on {}...",
            userTag, m + 1, months + 1, formatAmount(bonusAmount), bonusDate);

        transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
            null,
            Transaction.TransactionType.INCOME,
            bonusAmount,
            bonusDate,
            "Freelance Project Payout",
            state.getSavingsAccountId().toString(),
            null,
            null,
            refData.upiPaymentMode() != null ? String.valueOf(refData.upiPaymentMode().getId()) : null,
            salaryCategory != null ? String.valueOf(salaryCategory.getId()) : null,
            null
        ));
        state.creditSavings(bonusAmount);
        log.info("{} [Month {}/{}] [Income] Bonus credited. Live Savings Balance: ₹{}",
            userTag, m + 1, months + 1, formatAmount(state.getSavingsBalance()));

        delay("next operation: variable daily expenses");
      }

      // --- 6. Variable Expenses across the month ---
      final int expenseCount = faker.number().numberBetween(5, 9);
      log.info("{} [Month {}/{}] Scheduling {} variable daily expenses...", userTag, m + 1, months + 1, expenseCount);

      for (int k = 0; k < expenseCount; k++) {
        final int day = faker.number().numberBetween(1, daysInMonth + 1);
        final var txnDate = LocalDate.of(year, month, Math.min(day, daysInMonth));
        final var category = refData.getRandomCategory(faker);
        final var categoryName = category != null ? category.getName() : "Miscellaneous";
        final var expenseInfo = expenseCatalog.generateExpense(categoryName);

        log.info("{} [Month {}/{}] [Var Expense {}/{}] Posting '{}' (₹{}) under [{}] on {}...",
            userTag, m + 1, months + 1, k + 1, expenseCount,
            expenseInfo.description(), formatAmount(expenseInfo.amount()), categoryName, txnDate);

        safeCreateExpense(state, refData, category, expenseInfo, txnDate, userTag);

        final boolean hasMoreTxns = (k < expenseCount - 1) || (m < months);
        if (hasMoreTxns) {
          delay(String.format("next expense transaction (%d/%d)", k + 2, expenseCount));
        }
      }
    }
  }

  private void safeCreateExpense(
      UserSeedState state,
      SeedReferenceData refData,
      SystemCategory category,
      ExpenseCatalog.ExpenseDetail expenseInfo,
      LocalDate txnDate,
      String userTag
  ) {
    UUID accountId = state.getSavingsAccountId();
    UUID cardId = null;
    UUID paymentModeId = refData.upiPaymentMode() != null ? refData.upiPaymentMode().getId() : null;
    String modeName = "UPI / Savings";

    final int choice = faker.number().numberBetween(0, 4);
    if (choice == 0 && state.getCreditCardId() != null && refData.creditCardPaymentMode() != null) {
      accountId = null;
      cardId = state.getCreditCardId();
      paymentModeId = refData.creditCardPaymentMode().getId();
      modeName = "Credit Card";
    } else if (choice == 1 && state.getDebitCardId() != null && refData.debitCardPaymentMode() != null) {
      accountId = null;
      cardId = state.getDebitCardId();
      paymentModeId = refData.debitCardPaymentMode().getId();
      modeName = "Debit Card";
    } else if (choice == 2 && state.getCashAccountId() != null && refData.cashPaymentMode() != null) {
      accountId = state.getCashAccountId();
      paymentModeId = refData.cashPaymentMode().getId();
      modeName = "Cash";
    }

    final float amount = expenseInfo.amount();

    // 1. Cash low balance safeguard (< 200 remaining)
    if (accountId != null && accountId.equals(state.getCashAccountId()) && state.getCashBalance() - amount < 200.0f) {
      if (state.getSavingsBalance() >= 6000.0f) {
        final float withdrawal = 5000.0f;
        log.info("{} [Balance Guard] Cash low (₹{}). Triggering ATM cash withdrawal of ₹{} before expense...",
            userTag, formatAmount(state.getCashBalance()), formatAmount(withdrawal));

        transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
            null,
            Transaction.TransactionType.TRANSFER,
            withdrawal,
            txnDate,
            "ATM Cash Withdrawal",
            state.getSavingsAccountId().toString(),
            null,
            state.getCashAccountId().toString(),
            refData.cashPaymentMode() != null ? String.valueOf(refData.cashPaymentMode().getId()) : null,
            null,
            null
        ));
        state.transferSavingsToCash(withdrawal);
        log.info("{} [Balance Guard] ATM transfer done. Cash balance replenished to: ₹{}",
            userTag, formatAmount(state.getCashBalance()));

        delay("submitting guarded expense");
      } else {
        // Fallback to UPI from savings
        log.info("{} [Balance Guard] Insufficient savings (₹{}) to withdraw cash. Falling back payment to UPI.",
            userTag, formatAmount(state.getSavingsBalance()));
        accountId = state.getSavingsAccountId();
        cardId = null;
        paymentModeId = refData.upiPaymentMode() != null ? refData.upiPaymentMode().getId() : null;
        modeName = "UPI (Fallback)";
      }
    }

    // 2. Credit card low limit safeguard (< 500 remaining)
    if (cardId != null && cardId.equals(state.getCreditCardId()) && state.getCreditBalance() - amount < 500.0f) {
      log.info("{} [Balance Guard] Credit card limit low (₹{}). Falling back payment to UPI from Savings.",
          userTag, formatAmount(state.getCreditBalance()));
      accountId = state.getSavingsAccountId();
      cardId = null;
      paymentModeId = refData.upiPaymentMode() != null ? refData.upiPaymentMode().getId() : null;
      modeName = "UPI (Fallback)";
    }

    // 3. Savings account low balance safeguard (< 1000 remaining)
    final boolean isSavingsTarget = (accountId != null && accountId.equals(state.getSavingsAccountId()))
        || (cardId != null && cardId.equals(state.getDebitCardId()));

    if (isSavingsTarget && state.getSavingsBalance() - amount < 1000.0f) {
      final float topUp = 60000.0f;
      log.info("{} [Balance Guard] Savings low (₹{}). Injecting emergency Consulting Income top-up of ₹{}...",
          userTag, formatAmount(state.getSavingsBalance()), formatAmount(topUp));

      transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
          null,
          Transaction.TransactionType.INCOME,
          topUp,
          txnDate,
          "Consulting Payout / Returns",
          state.getSavingsAccountId().toString(),
          null,
          null,
          refData.upiPaymentMode() != null ? String.valueOf(refData.upiPaymentMode().getId()) : null,
          null,
          null
      ));
      state.creditSavings(topUp);
      log.info("{} [Balance Guard] Top-up credited. Savings balance restored to: ₹{}",
          userTag, formatAmount(state.getSavingsBalance()));

      delay("submitting guarded expense");
    }

    // 4. Create the Expense Transaction
    assert accountId != null;
    assert paymentModeId != null;

    transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
        null,
        Transaction.TransactionType.EXPENSE,
        amount,
        txnDate,
        expenseInfo.description(),
        accountId.toString(),
        cardId.toString(),
        null,
        paymentModeId.toString(),
        category != null ? String.valueOf(category.getId()) : null,
        null
    ));

    // 5. Update local in-memory ledger
    if (cardId != null && cardId.equals(state.getCreditCardId())) {
      state.chargeCredit(amount);
    } else if (accountId != null && accountId.equals(state.getCashAccountId())) {
      state.chargeCash(amount);
    } else {
      state.chargeSavings(amount);
    }

    log.info("{} [Expense Recorded] '{}' of ₹{} via [{}]. Updated -> Savings: ₹{}, Cash: ₹{}, Credit Avail: ₹{}",
        userTag, expenseInfo.description(), formatAmount(amount), modeName,
        formatAmount(state.getSavingsBalance()),
        formatAmount(state.getCashBalance()),
        formatAmount(state.getCreditBalance())
    );
  }

  private void delay(String nextOperationDesc) {
    final int delaySeconds = faker.number().numberBetween(5, 11);
    log.info("⏳ [Rate-Limit Safety] Waiting {}s before {}...", delaySeconds, nextOperationDesc);
    try {
      TimeUnit.SECONDS.sleep(delaySeconds);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Delay interrupted while waiting before: {}", nextOperationDesc);
    }
  }

  private String formatAmount(float amount) {
    return String.format("%,.2f", amount);
  }

  private SeedReferenceData loadReferenceData() {
    final var banks = bankRepo.findAll().stream().map(bankMapper::toDto).toList();
    final var paymentModes = paymentModeRepo.findAll();
    final var categories = systemCategoryRepo.findAll();

    final var upiMode = findPaymentModeByName(paymentModes, "UPI / NetBanking");
    final var cashMode = findPaymentModeByName(paymentModes, "Cash");
    final var debitCardMode = findPaymentModeByName(paymentModes, "Debit Card");
    final var creditCardMode = findPaymentModeByName(paymentModes, "Credit Card");

    return new SeedReferenceData(
        banks,
        upiMode,
        cashMode != null ? cashMode : upiMode,
        debitCardMode != null ? debitCardMode : upiMode,
        creditCardMode != null ? creditCardMode : upiMode,
        categories
    );
  }

  private PaymentMode findPaymentModeByName(List<PaymentMode> modes, String name) {
    return modes.stream()
        .filter(pm -> pm.getName() != null && pm.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElse(!modes.isEmpty() ? modes.get(0) : null);
  }
}
