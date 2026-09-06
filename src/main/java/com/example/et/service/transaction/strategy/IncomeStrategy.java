package com.example.et.service.transaction.strategy;

import com.example.et.controller.dto.transaction.TransactionResponseDto;
import com.example.et.mapper.PaymentModeMapper;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.Transaction;
import com.example.et.repo.TransactionRepo;
import com.example.et.service.account.AccountService;
import com.example.et.service.ai.parsetask.AiParseTaskService;
import com.example.et.service.transaction.TransactionContext;
import com.example.et.service.transaction.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IncomeStrategy implements  TransactionStrategy {
  private final AccountService accountService;
  private final TransactionRepo transactionRepo;
  private final AiParseTaskService aiParseTaskService;
  private final PaymentModeMapper paymentModeMapper;

  @Override
  public TransactionResponseDto execute(TransactionContext transactionContext) {
    final var userId = transactionContext.userId();
    final var user = AppUser.ofId(userId);
    final var account = accountService.getAccount(UUID.fromString(userId), transactionContext.requestDto().accountId());

    account.credit(transactionContext.requestDto().amount());
    accountService.saveAccount(account);

    final var transaction = Transaction.builder()
        .appUser(user)
        .account(account)
        .type(transactionContext.requestDto().type())
        .amount(transactionContext.requestDto().amount())
        .transactionDate(transactionContext.requestDto().transactionDate())
        .description(transactionContext.requestDto().description())
        .paymentMode(paymentModeMapper.toEntity(transactionContext.paymentMode()))
        .transactionCategory(transactionContext.systemCategory())
        .build();

    return TransactionServiceImpl.toDto(transactionRepo.save(transaction));
  }

  @Override
  public void delete(String userId, Transaction transaction) {
    if (transaction.getAccount() != null) {
      final var account = transaction.getAccount();
      account.debit(transaction.getAmount());
      accountService.saveAccount(account);
    }

    aiParseTaskService.unlinkTransaction(transaction.getId());
    transactionRepo.delete(transaction);
  }

  @Override
  public Transaction.TransactionType getType() {
    return Transaction.TransactionType.INCOME;
  }
}
