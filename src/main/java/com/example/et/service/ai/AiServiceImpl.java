package com.example.et.service.ai;

import com.example.et.controller.dto.AiInputDto;
import com.example.et.controller.dto.AiTaskDto;
import com.example.et.model.ai.AiParsingTask;
import com.example.et.model.ai.Status;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.PaymentMode;
import com.example.et.model.core.SystemCategory;
import com.example.et.model.core.Transaction;
import com.example.et.repo.AccountRepo;
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
import java.util.List;

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
