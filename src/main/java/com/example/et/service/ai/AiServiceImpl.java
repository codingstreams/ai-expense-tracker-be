package com.example.et.service.ai;

import com.example.et.controller.dto.AiInputDto;
import com.example.et.controller.dto.AiInsightDto;
import com.example.et.controller.dto.AiTaskDto;
import com.example.et.model.ai.AiInsight;
import com.example.et.model.ai.AiParsingTask;
import com.example.et.model.ai.Status;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.PaymentMode;
import com.example.et.model.core.SystemCategory;
import com.example.et.model.core.Transaction;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.AiInsightRepo;
import com.example.et.repo.AppUserConfigRepo;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.repo.SystemCategoryRepo;
import com.example.et.repo.TransactionRepo;
import com.example.et.service.ai.parsetask.AiParseTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {
  private final AiParseTaskService aiParseTaskService;
  private final ChatClient chatClient;
  private final TransactionRepo transactionRepo;
  private final AccountRepo accountRepo;
  private final PaymentModeRepo paymentModeRepo;
  private final SystemCategoryRepo systemCategoryRepo;
  private final AppUserConfigRepo appUserConfigRepo;
  private final AiInsightRepo aiInsightRepo;

  public record ParsedTransaction(
      Float amount,
      Transaction.TransactionType type,
      String description,
      LocalDate transactionDate,
      String category,
      String paymentMode
  ) {}

  @Override
  public AiTaskDto save(String appUserId, AiInputDto requestBody) {
    AiParsingTask task = AiParsingTask.builder()
        .appUser(AppUser.ofId(appUserId))
        .rawInput(requestBody.rawText())
        .status(Status.PENDING)
        .build();

    AiParsingTask saved = aiParseTaskService.save(task);
    return new AiTaskDto(saved.getId().toString(), "Task created successfully");
  }

  @Override
  @Transactional
  public void parse(AiParsingTask task) {
    try {
      String systemPrompt = """
          You are a financial transaction parser. Extract transaction details from user natural language input.
          Infer type as EXPENSE or INCOME.
          Extract payment mode if mentioned (e.g. UPI, CASH, CREDIT_CARD, DEBIT_CARD, NET_BANKING).
          Extract category if mentioned (e.g. Food, Groceries, Shopping, Travel, Entertainment, Salary, Bills).
          Today's date is %s.
          """.formatted(LocalDate.now());

      ParsedTransaction parsed = chatClient.prompt()
          .system(systemPrompt)
          .user(task.getRawInput())
          .call()
          .entity(ParsedTransaction.class);

      if (parsed == null || parsed.amount() == null) {
        task.setStatus(Status.FAILED);
        task.setErrorMessage("Could not parse transaction details from input");
        aiParseTaskService.save(task);
        return;
      }

      PaymentMode paymentMode = resolvePaymentMode(task.getAppUser(), parsed.paymentMode());
      SystemCategory category = resolveCategory(parsed.category());
      Account account = resolveAccount(task.getAppUser(), paymentMode);

      Transaction.TransactionType txnType = parsed.type() != null ? parsed.type() : Transaction.TransactionType.EXPENSE;
      Float amount = parsed.amount();

      if (account != null) {
        if (txnType == Transaction.TransactionType.EXPENSE) {
          account.setBalance(account.getBalance() - amount);
        } else if (txnType == Transaction.TransactionType.INCOME) {
          account.setBalance(account.getBalance() + amount);
        }
        accountRepo.save(account);
      }

      Transaction transaction = Transaction.builder()
          .amount(txnType == Transaction.TransactionType.EXPENSE ? -amount : amount)
          .type(txnType)
          .description(parsed.description() != null ? parsed.description() : task.getRawInput())
          .transactionDate(parsed.transactionDate() != null ? parsed.transactionDate() : LocalDate.now())
          .appUser(task.getAppUser())
          .account(account)
          .paymentMode(paymentMode)
          .transactionCategory(category)
          .build();

      Transaction savedTx = transactionRepo.save(transaction);

      task.setTransaction(savedTx);
      task.setContent(parsed.toString());
      task.setStatus(Status.COMPLETED);
      aiParseTaskService.save(task);
    } catch (Exception e) {
      log.error("Failed to parse AI task id: {}", task.getId(), e);
      task.setStatus(Status.FAILED);
      task.setErrorMessage(e.getMessage());
      aiParseTaskService.save(task);
    }
  }

  @Override
  @Transactional
  public AiInsightDto generateInsights(String appUserId) {
    UUID userUuid = UUID.fromString(appUserId);

    long weeklyCount = aiInsightRepo.countByAppUserIdAndCreatedAtGreaterThanEqual(
        userUuid, LocalDateTime.now().minusDays(7));
    if (weeklyCount >= 2) {
      throw new RuntimeException("Weekly AI insight limit reached (maximum 2 per week).");
    }

    long monthlyCount = aiInsightRepo.countByAppUserIdAndCreatedAtGreaterThanEqual(
        userUuid, LocalDateTime.now().minusDays(30));
    if (monthlyCount >= 4) {
      throw new RuntimeException("Monthly AI insight limit reached (maximum 4 per month).");
    }

    LocalDate now = LocalDate.now();
    LocalDate startDate = now.withDayOfMonth(1);
    LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

    List<Transaction> transactions = transactionRepo.findByAppUserIdAndTransactionDateBetween(
        userUuid, startDate, endDate);

    String periodName = now.getMonth().name() + " " + now.getYear();

    if (transactions.isEmpty()) {
      return new AiInsightDto(
          periodName,
          LocalDateTime.now(),
          "No transactions recorded for the current period.",
          null,
          List.of(),
          List.of("Start logging your daily transactions to receive AI insights.")
      );
    }

    String dataSummary = transactions.stream()
        .map(t -> String.format("- %s: %.2f (%s, %s, %s)",
            t.getTransactionDate(),
            Math.abs(t.getAmount()),
            t.getType(),
            t.getTransactionCategory() != null ? t.getTransactionCategory().getName() : "Uncategorized",
            t.getDescription()))
        .collect(Collectors.joining("\n"));

    String systemPrompt = """
        You are a personal financial advisor. Analyze the given month's transactions and provide structured insights.
        - summary: 2 concise sentences on spending pace and savings health.
        - topSpendingCategory: the single category with highest spend (name, percentage of total expenses, and brief insight).
        - anomalies: list of unusual transactions or rapid spend patterns.
        - actionableTips: 2-3 specific, realistic saving recommendations.
        """;

    try {
      AiInsightDto insight = chatClient.prompt()
          .system(systemPrompt)
          .user("Period: " + periodName + "\nTransactions:\n" + dataSummary)
          .call()
          .entity(AiInsightDto.class);

      if (insight != null) {
        AiInsight entity = AiInsight.builder()
            .appUser(AppUser.ofId(appUserId))
            .period(periodName)
            .summary(insight.summary())
            .topSpendingCategory(insight.topSpendingCategory() != null ? insight.topSpendingCategory().category() : null)
            .topSpendingPercentage(insight.topSpendingCategory() != null && insight.topSpendingCategory().percentage() != null ? insight.topSpendingCategory().percentage().floatValue() : null)
            .topSpendingInsight(insight.topSpendingCategory() != null ? insight.topSpendingCategory().insight() : null)
            .anomalies(insight.anomalies() != null ? String.join(";", insight.anomalies()) : null)
            .actionableTips(insight.actionableTips() != null ? String.join(";", insight.actionableTips()) : null)
            .build();

        aiInsightRepo.save(entity);

        return new AiInsightDto(
            periodName,
            LocalDateTime.now(),
            insight.summary(),
            insight.topSpendingCategory(),
            insight.anomalies() != null ? insight.anomalies() : List.of(),
            insight.actionableTips() != null ? insight.actionableTips() : List.of()
        );
      }
    } catch (Exception e) {
      log.error("Failed to generate AI insights for user: {}", appUserId, e);
    }

    return new AiInsightDto(
        periodName,
        LocalDateTime.now(),
        "Unable to generate AI insights at this time.",
        null,
        List.of(),
        List.of()
    );
  }

  @Override
  public AiInsightDto getLatestInsight(String appUserId) {
    UUID userUuid = UUID.fromString(appUserId);
    return aiInsightRepo.findFirstByAppUserIdOrderByCreatedAtDesc(userUuid)
        .map(this::toDto)
        .orElse(null);
  }

  private AiInsightDto toDto(AiInsight entity) {
    return new AiInsightDto(
        entity.getPeriod(),
        entity.getCreatedAt(),
        entity.getSummary(),
        entity.getTopSpendingCategory() != null ? new AiInsightDto.TopCategory(
            entity.getTopSpendingCategory(),
            entity.getTopSpendingPercentage() != null ? entity.getTopSpendingPercentage().doubleValue() : null,
            entity.getTopSpendingInsight()
        ) : null,
        entity.getAnomalies() != null && !entity.getAnomalies().isBlank()
            ? List.of(entity.getAnomalies().split(";"))
            : List.of(),
        entity.getActionableTips() != null && !entity.getActionableTips().isBlank()
            ? List.of(entity.getActionableTips().split(";"))
            : List.of()
    );
  }

  private Account resolveAccount(AppUser appUser, PaymentMode paymentMode) {
    if (appUser == null || appUser.getId() == null) {
      return null;
    }
    List<Account> accounts = accountRepo.findByAppUserId(appUser.getId());
    if (accounts.isEmpty()) {
      return null;
    }

    if (paymentMode != null && "CASH".equalsIgnoreCase(paymentMode.getName())) {
      var cashAcc = accounts.stream().filter(a -> a.getAccountType() == Account.AccountType.CASH).findFirst();
      if (cashAcc.isPresent()) {
        return cashAcc.get();
      }
    }

    return accounts.stream()
        .filter(a -> a.getAccountType() == Account.AccountType.SAVINGS)
        .findFirst()
        .orElse(accounts.get(0));
  }

  private PaymentMode resolvePaymentMode(AppUser appUser, String extractedPaymentMode) {
    if (extractedPaymentMode != null && !extractedPaymentMode.isBlank()) {
      var found = paymentModeRepo.findByNameIgnoreCase(extractedPaymentMode.trim());
      if (found.isPresent()) {
        return found.get();
      }
    }

    if (appUser != null && appUser.getId() != null) {
      var userConfig = appUserConfigRepo.findByUserId(appUser.getId());
      if (userConfig.isPresent() && userConfig.get().getPaymentMode() != null) {
        return userConfig.get().getPaymentMode();
      }
    }

    return paymentModeRepo.findByNameIgnoreCase("UPI")
        .or(() -> paymentModeRepo.findByNameIgnoreCase("CASH"))
        .or(() -> paymentModeRepo.findAll().stream().findFirst())
        .orElse(null);
  }

  private SystemCategory resolveCategory(String extractedCategory) {
    if (extractedCategory != null && !extractedCategory.isBlank()) {
      return systemCategoryRepo.findFirstByNameContainingIgnoreCase(extractedCategory.trim())
          .or(() -> systemCategoryRepo.findByNameIgnoreCase(extractedCategory.trim()))
          .orElse(null);
    }
    return null;
  }
}
