package com.example.et.module.transaction.internal.strategy;

import com.example.et.module.account.AccountMapper;
import com.example.et.module.account.AccountService;
import com.example.et.module.ai.parser.AiParseTaskService;
import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.transaction.Transaction;
import com.example.et.module.transaction.TransactionContext;
import com.example.et.module.transaction.TransactionMapper;
import com.example.et.module.transaction.dto.TransactionResponseDto;
import com.example.et.module.transaction.internal.TransactionRepo;
import com.example.et.module.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IncomeStrategy implements TransactionStrategy {
  private final AccountService accountService;
  private final TransactionRepo transactionRepo;
  private final AiParseTaskService aiParseTaskService;
  private final PaymentModeMapper paymentModeMapper;
  private final TransactionMapper transactionMapper;
  private final AccountMapper accountMapper;

  @Override
  public TransactionResponseDto execute(TransactionContext transactionContext) {
    final var userId = transactionContext.userId();
    final var user = AppUser.ofId(userId);
    final var account = accountMapper.toEntity(accountService.getAccount(userId, transactionContext.requestDto().accountId()));

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

    return transactionMapper.toResponseDto(transactionRepo.save(transaction));
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
