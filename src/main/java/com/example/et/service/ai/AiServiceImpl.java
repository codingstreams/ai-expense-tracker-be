package com.example.et.service.ai;

import com.example.et.controller.dto.AiInputDto;
import com.example.et.controller.dto.AiTaskDto;
import com.example.et.controller.dto.TransactionRequestDto;
import com.example.et.model.ai.AiParsingTask;
import com.example.et.model.core.*;
import com.example.et.repo.AppUserConfigRepo;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.repo.SysCategoryRepo;
import com.example.et.service.account.AccountService;
import com.example.et.service.ai.parsetask.AiParseTaskService;
import com.example.et.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {
  private final AiParseTaskService aiParseTaskService;
  private final ChatClient chatClient;
  private final PaymentModeRepo paymentModeRepo;
  private final AppUserConfigRepo appUserConfigRepo;
  private final SysCategoryRepo sysCategoryRepo;
  private final AccountService accountService;
  private final TransactionService transactionService;

  public record ParsedTransaction(
      Float amount,
      Transaction.TransactionType type,
      String description,
      LocalDate transactionDate,
      String category,
      String paymentMode
  ) {
  }

  @Override
  public AiTaskDto save(String userId, AiInputDto requestBody) {

    final var task = AiParsingTask.builder()
        .appUser(AppUser.ofId(userId))
        .rawInput(requestBody.rawText())
        .status(AiParsingTask.Status.PENDING)
        .build();

    final var saved = aiParseTaskService.save(task);
    return new AiTaskDto(saved.getId().toString(), "Task created successfully");
  }

  @Override
  public void parse(AiParsingTask task) {
    log.info("Parsing task started");
    try {
      final var systemPrompt = """
          You are a financial transaction parser. Extract transaction details from user natural language input.
          Infer type as EXPENSE or INCOME.
          Extract payment mode if mentioned (e.g. UPI, CASH, CREDIT_CARD, DEBIT_CARD, NET_BANKING).
          Extract category if mentioned (e.g. Food, Groceries, Shopping, Travel, Entertainment, Salary, Bills).
          Today's date is %s.
          """.formatted(LocalDate.now());

      final var parsed = chatClient.prompt()
          .system(systemPrompt)
          .user(task.getRawInput())
          .call()
          .entity(ParsedTransaction.class);

      if (parsed == null || parsed.amount() == null) {
        task.setStatus(AiParsingTask.Status.FAILED);
        task.setErrorMessage("Could not parse transaction details from input");
        aiParseTaskService.save(task);
        return;
      }

      final var paymentMode = resolvePaymentMode(task.getAppUser(), parsed.paymentMode());
      final var category = resolveCategory(parsed.category());
      final var account = resolveAccount(task.getAppUser(), paymentMode);

      final var txnDate = Optional.of(parsed)
          .map(ParsedTransaction::transactionDate)
          .orElseGet(LocalDate::now);

      final var userId = Optional.of(task)
          .map(AiParsingTask::getAppUser)
          .map(AppUser::getId)
          .map(String::valueOf)
          .orElse(null);

      final var accountId = Optional.ofNullable(account)
          .map(Account::getId)
          .orElse(null);

      final var paymentModeId = Optional.ofNullable(paymentMode)
          .map(PaymentMode::getId)
          .orElse(null);

      final var categoryId = Optional.ofNullable(category)
          .map(SystemCategory::getId)
          .orElse(null);

      final var txnType = Optional.of(parsed)
          .map(ParsedTransaction::type)
          .orElse(Transaction.TransactionType.EXPENSE);

      final var description = Optional.of(parsed)
          .map(ParsedTransaction::description)
          .orElse("");

      final var amount = Optional.of(parsed)
          .map(ParsedTransaction::amount)
          .orElse(null);

      if (userId == null || accountId == null || paymentModeId == null || categoryId == null) {
        throw new IllegalArgumentException("Required transaction details (user, account, payment mode, or category) cannot be null.");
      }

      final var requestDto = new TransactionRequestDto(
          null,
          txnType,
          amount,
          txnDate,
          description,
          accountId,
          null,
          null,
          paymentModeId,
          categoryId,
          null
      );

      transactionService.createTransaction(userId, requestDto);

      task.setContent(parsed.toString());
      task.setStatus(AiParsingTask.Status.COMPLETED);
      aiParseTaskService.save(task);

      log.info("Parsing task ended");
    } catch (Exception e) {
      log.info("Parsing task failed");
      task.setStatus(AiParsingTask.Status.FAILED);
      task.setErrorMessage(e.getMessage());
      aiParseTaskService.save(task);
    }
  }

  private Account resolveAccount(AppUser appUser, PaymentMode paymentMode) {
    if (appUser == null || appUser.getId() == null) {
      return null;
    }
    List<Account> accounts = accountService.getUserAccountList(String.valueOf(appUser.getId()));
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
    // Payment Mode from parsed task
    if (extractedPaymentMode != null && !extractedPaymentMode.isBlank()) {
      var found = paymentModeRepo.findByNameIgnoreCase(extractedPaymentMode.trim());
      if (found.isPresent()) {
        return found.get();
      }
    }

    //
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
      return sysCategoryRepo.findFirstByNameContainingIgnoreCase(extractedCategory.trim())
          .orElse(null);
    }
    return null;
  }
}
