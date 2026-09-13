package com.example.et.module.transaction.internal.strategy;

import com.example.et.module.account.Account;
import com.example.et.module.account.AccountMapper;
import com.example.et.module.account.AccountService;
import com.example.et.module.ai.parser.AiParseTaskService;
import com.example.et.module.card.CardService;
import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.transaction.Transaction;
import com.example.et.module.transaction.TransactionContext;
import com.example.et.module.transaction.TransactionMapper;
import com.example.et.module.transaction.dto.TransactionResponseDto;
import com.example.et.module.transaction.internal.TransactionRepo;
import com.example.et.module.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExpenseStrategy implements TransactionStrategy {
  private final CardService cardService;
  private final AccountService accountService;
  private final TransactionRepo transactionRepo;
  private final AiParseTaskService aiParseTaskService;
  private final PaymentModeMapper paymentModeMapper;
  private final TransactionMapper transactionMapper;
  private final AccountMapper accountMapper;

  private Account resolveAccount(String userId, String accountId, String cardId) {
    if (cardId != null) {
      final var card = cardService.getUserCard(userId, UUID.fromString(cardId));
      if (card.getAccount() == null) {
        throw new RuntimeException("Card is not linked to any account");
      }
      return card.getAccount();
    }
    if (accountId != null) {
      return accountMapper.toEntity(accountService.getAccount(userId, accountId));
    }
    throw new RuntimeException("Either accountId or cardId must be provided");
  }

  @Override
  public TransactionResponseDto execute(TransactionContext transactionContext) {
    final var userId = transactionContext.userId();
    final var user = AppUser.ofId(userId);
    final var account = resolveAccount(userId, transactionContext.requestDto().accountId().toString(), transactionContext.requestDto().cardId().toString());

    account.debit(transactionContext.requestDto().amount());
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
      account.credit(transaction.getAmount());
      accountService.saveAccount(account);
    }

    aiParseTaskService.unlinkTransaction(transaction.getId());
    transactionRepo.delete(transaction);
  }

  @Override
  public Transaction.TransactionType getType() {
    return Transaction.TransactionType.EXPENSE;
  }
}
