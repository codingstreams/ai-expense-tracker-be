package com.example.et.service.dataseeder;

import com.example.et.controller.dto.account.AccountDto;
import com.example.et.controller.dto.appuser.UpdateUserDetailsDto;
import com.example.et.controller.dto.auth.CreateUserReq;
import com.example.et.controller.dto.card.CardDto;
import com.example.et.controller.dto.card.UserCards;
import com.example.et.controller.dto.dashboard.OnboardUserDto;
import com.example.et.controller.dto.transaction.TransactionRequestDto;
import com.example.et.mapper.BankMapper;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUserConfig;
import com.example.et.model.core.Card;
import com.example.et.model.core.PaymentMode;
import com.example.et.model.core.SystemCategory;
import com.example.et.model.core.Transaction;
import com.example.et.repo.BankRepo;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.repo.SysCategoryRepo;
import com.example.et.service.account.AccountService;
import com.example.et.service.appuser.AppUserService;
import com.example.et.service.auth.AuthService;
import com.example.et.service.card.CardService;
import com.example.et.service.dashboard.DashboardService;
import com.example.et.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
    log.info("Starting bulk creation of {} users", count);
    for (int i = 0; i < count; i++) {
      final var request = new CreateUserReq(
          faker.name().fullName(),
          faker.internet().emailAddress(),
          DEFAULT_USER_PASSWORD
      );
      authService.register(request);
    }
    log.info("Completed bulk creation of {} users", count);
    return count;
  }

  @Override
  public int seedData(int noOfUsers, int monthsOfTransactions) {
    final long startTime = System.currentTimeMillis();
    log.info("Beginning full data seeding: {} users, {} months each", noOfUsers, monthsOfTransactions);

    // Cache static reference data once for the entire batch to eliminate redundant cloud DB queries
    final SeedReferenceData refData = loadReferenceData();

    int seededUsers = 0;
    for (int i = 0; i < noOfUsers; i++) {
      final String name = faker.name().fullName();
      final String email = faker.internet().emailAddress();
      try {
        log.info("Seeding user {}/{} [{} - {}]", i + 1, noOfUsers, name, email);
        seedUserFullJourney(name, email, monthsOfTransactions, refData);
        seededUsers++;
      } catch (Exception e) {
        log.error("Failed seeding full data for user: {} ({})", email, e.getMessage(), e);
      }
    }

    final long duration = System.currentTimeMillis() - startTime;
    log.info("Finished data seeding: {}/{} users seeded successfully in {} ms", seededUsers, noOfUsers, duration);
    return seededUsers;
  }

  public void seedUserFullJourney(String name, String email, int monthsOfTransactions) {
    seedUserFullJourney(name, email, monthsOfTransactions, loadReferenceData());
  }

  public void seedUserFullJourney(String name, String email, int monthsOfTransactions, SeedReferenceData refData) {
    // 1. Register User & resolve UUID
    authService.register(new CreateUserReq(name, email, DEFAULT_JOURNEY_PASSWORD));
    final var appUser = appUserService.getUserByEmail(email);
    final var userId = appUser.getId().toString();

    // 2. Select Banks from pre-cached reference data
    final var primaryBank = refData.getRandomBank(faker);
    final var secondaryBank = refData.getSecondaryBank(primaryBank);

    // 3. Onboard User with Savings Account & User Config
    final float initialSavingsBalance = (float) faker.number().randomDouble(2, 50000, 150000);
    final float initialCashBalance = (float) faker.number().randomDouble(2, 3000, 10000);

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
      log.warn("Skipping card and transaction seeding for {} due to missing savings account or bank", userId);
      return;
    }

    // 4. Issue Debit and Credit Cards
    final float initialCreditLimit = (float) faker.number().randomDouble(2, 75000, 200000);

    final var debitCardDto = new CardDto(
        null,
        Card.CardType.DEBIT_CARD,
        String.valueOf(faker.number().numberBetween(1000, 9999)),
        savingsAccount.id(),
        null,
        bankMapper.toEntity(primaryBank)
    );

    final var creditCardDto = new CardDto(
        null,
        Card.CardType.CREDIT_CARD,
        String.valueOf(faker.number().numberBetween(1000, 9999)),
        null,
        initialCreditLimit,
        bankMapper.toEntity(secondaryBank != null ? secondaryBank : primaryBank)
    );

    // Reuse returned card list to avoid redundant remote read queries
    final var createdCards = cardService.addCards(userId, new UserCards(List.of(debitCardDto, creditCardDto)));

    final UUID debitCardId = createdCards.stream()
        .filter(c -> c.cardType() == Card.CardType.DEBIT_CARD)
        .map(CardDto::id)
        .findFirst()
        .orElse(null);

    final UUID creditCardId = createdCards.stream()
        .filter(c -> c.cardType() == Card.CardType.CREDIT_CARD)
        .map(CardDto::id)
        .findFirst()
        .orElse(null);

    // 5. Query Cash Account (single read)
    final var cashAccountDto = accountService.getUserCashAccountDetails(userId);
    final UUID cashAccountId = cashAccountDto != null ? cashAccountDto.id() : null;

    // 6. Initialize In-Memory Ledger for O(1) balance tracking without cloud DB round-trips
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

    // 7. Seed Multi-Month Transactions
    seedTransactionsForUser(userState, refData, monthsOfTransactions);
  }

  private void seedTransactionsForUser(UserSeedState state, SeedReferenceData refData, int months) {
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

      // Day 1: Monthly Salary Credit
      final var salaryDate = LocalDate.of(year, month, Math.min(1, daysInMonth));
      final var salaryCategory = refData.findCategoryByKeywords("Investments", "Miscellaneous");
      final float salaryAmount = (float) faker.number().randomDouble(2, 80000, 140000);

      transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
          null,
          Transaction.TransactionType.INCOME,
          salaryAmount,
          salaryDate,
          "Monthly Salary Credit",
          state.getSavingsAccountId(),
          null,
          null,
          refData.upiPaymentMode() != null ? refData.upiPaymentMode().getId() : null,
          salaryCategory != null ? salaryCategory.getId() : null,
          null
      ));
      state.creditSavings(salaryAmount);

      // Day 2: ATM Cash Withdrawal (transfer savings -> cash)
      if (state.getCashAccountId() != null && daysInMonth >= 2 && state.getSavingsBalance() >= 10000.0f) {
        final var transferDate = LocalDate.of(year, month, 2);
        final float withdrawalAmount = 5000.0f;

        transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
            null,
            Transaction.TransactionType.TRANSFER,
            withdrawalAmount,
            transferDate,
            "ATM Cash Withdrawal",
            state.getSavingsAccountId(),
            null,
            state.getCashAccountId(),
            refData.cashPaymentMode() != null ? refData.cashPaymentMode().getId() : null,
            null,
            null
        ));
        state.transferSavingsToCash(withdrawalAmount);
      }

      // Day 3: Rent / EMI
      if (daysInMonth >= 3) {
        final var rentCategory = refData.findCategoryByKeywords("Rent/EMI", "Utilities");
        safeCreateExpense(
            state,
            refData,
            rentCategory,
            new ExpenseCatalog.ExpenseDetail("Monthly House Rent", (float) faker.number().randomDouble(2, 15000, 30000)),
            LocalDate.of(year, month, 3)
        );
      }

      // Day 10: Utilities
      if (daysInMonth >= 10) {
        final var utilCategory = refData.findCategoryByKeywords("Utilities (Electricity/Water)", "Utilities");
        safeCreateExpense(
            state,
            refData,
            utilCategory,
            new ExpenseCatalog.ExpenseDetail("Electricity & Water Bill", (float) faker.number().randomDouble(2, 1200, 4500)),
            LocalDate.of(year, month, 10)
        );
      }

      // Day 15: Freelance / Bonus Income
      if (daysInMonth >= 15) {
        final var bonusDate = LocalDate.of(year, month, 15);
        final float bonusAmount = (float) faker.number().randomDouble(2, 15000, 35000);

        transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
            null,
            Transaction.TransactionType.INCOME,
            bonusAmount,
            bonusDate,
            "Freelance Project Payout",
            state.getSavingsAccountId(),
            null,
            null,
            refData.upiPaymentMode() != null ? refData.upiPaymentMode().getId() : null,
            salaryCategory != null ? salaryCategory.getId() : null,
            null
        ));
        state.creditSavings(bonusAmount);
      }

      // Random variable expenses across the month
      final int expenseCount = faker.number().numberBetween(8, 16);
      for (int k = 0; k < expenseCount; k++) {
        final int day = faker.number().numberBetween(1, daysInMonth + 1);
        final var txnDate = LocalDate.of(year, month, Math.min(day, daysInMonth));
        final var category = refData.getRandomCategory(faker);
        final var categoryName = category != null ? category.getName() : "Miscellaneous";
        final var expenseInfo = expenseCatalog.generateExpense(categoryName);

        safeCreateExpense(state, refData, category, expenseInfo, txnDate);
      }
    }
  }

  private void safeCreateExpense(
      UserSeedState state,
      SeedReferenceData refData,
      SystemCategory category,
      ExpenseCatalog.ExpenseDetail expenseInfo,
      LocalDate txnDate
  ) {
    UUID accountId = state.getSavingsAccountId();
    UUID cardId = null;
    UUID paymentModeId = refData.upiPaymentMode() != null ? refData.upiPaymentMode().getId() : null;

    final int choice = faker.number().numberBetween(0, 4);
    if (choice == 0 && state.getCreditCardId() != null && refData.creditCardPaymentMode() != null) {
      accountId = null;
      cardId = state.getCreditCardId();
      paymentModeId = refData.creditCardPaymentMode().getId();
    } else if (choice == 1 && state.getDebitCardId() != null && refData.debitCardPaymentMode() != null) {
      accountId = null;
      cardId = state.getDebitCardId();
      paymentModeId = refData.debitCardPaymentMode().getId();
    } else if (choice == 2 && state.getCashAccountId() != null && refData.cashPaymentMode() != null) {
      accountId = state.getCashAccountId();
      paymentModeId = refData.cashPaymentMode().getId();
    }

    final float amount = expenseInfo.amount();

    // 1. Cash account low balance guard (< 200 remaining)
    if (accountId != null && accountId.equals(state.getCashAccountId()) && state.getCashBalance() - amount < 200.0f) {
      if (state.getSavingsBalance() >= 6000.0f) {
        final float withdrawal = 5000.0f;
        transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
            null,
            Transaction.TransactionType.TRANSFER,
            withdrawal,
            txnDate,
            "ATM Cash Withdrawal",
            state.getSavingsAccountId(),
            null,
            state.getCashAccountId(),
            refData.cashPaymentMode() != null ? refData.cashPaymentMode().getId() : null,
            null,
            null
        ));
        state.transferSavingsToCash(withdrawal);
      } else {
        // Fallback to UPI from savings
        accountId = state.getSavingsAccountId();
        cardId = null;
        paymentModeId = refData.upiPaymentMode() != null ? refData.upiPaymentMode().getId() : null;
      }
    }

    // 2. Credit card low limit guard (< 500 remaining)
    if (cardId != null && cardId.equals(state.getCreditCardId()) && state.getCreditBalance() - amount < 500.0f) {
      accountId = state.getSavingsAccountId();
      cardId = null;
      paymentModeId = refData.upiPaymentMode() != null ? refData.upiPaymentMode().getId() : null;
    }

    // 3. Savings account low balance guard (< 1000 remaining)
    final boolean isSavingsTarget = (accountId != null && accountId.equals(state.getSavingsAccountId()))
        || (cardId != null && cardId.equals(state.getDebitCardId()));

    if (isSavingsTarget && state.getSavingsBalance() - amount < 1000.0f) {
      final float topUp = 60000.0f;
      transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
          null,
          Transaction.TransactionType.INCOME,
          topUp,
          txnDate,
          "Consulting Payout / Returns",
          state.getSavingsAccountId(),
          null,
          null,
          refData.upiPaymentMode() != null ? refData.upiPaymentMode().getId() : null,
          null,
          null
      ));
      state.creditSavings(topUp);
    }

    // 4. Create the Expense Transaction
    transactionsService.createTransaction(state.getUserId(), new TransactionRequestDto(
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

    // 5. Update local in-memory ledger in O(1)
    if (cardId != null && cardId.equals(state.getCreditCardId())) {
      state.chargeCredit(amount);
    } else if (accountId != null && accountId.equals(state.getCashAccountId())) {
      state.chargeCash(amount);
    } else {
      state.chargeSavings(amount);
    }
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
