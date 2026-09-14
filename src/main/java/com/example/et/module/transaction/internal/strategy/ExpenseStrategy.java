package com.example.et.module.transaction.internal.strategy;

import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.module.account.Account;
import com.example.et.module.account.AccountService;
import com.example.et.module.ai.parser.AiParseTaskService;
import com.example.et.module.card.CardService;
import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.transaction.Transaction;
import com.example.et.module.transaction.TransactionContext;
import com.example.et.module.transaction.TransactionMapper;
import com.example.et.module.transaction.dto.TransactionDetailsResponse;
import com.example.et.module.transaction.internal.TransactionRepo;
import com.example.et.module.user.AppUser;
import io.micrometer.common.util.StringUtils;
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

  private Account resolveAccount(String userId, String accountId, String cardId) {
    if (cardId != null && !StringUtils.isBlank(cardId)) {
      final var card = cardService.getUserCard(userId, UUID.fromString(cardId));
      if (card.getAccount() == null) {
        throw new ApiException(ErrorCode.CARD_NOT_LINKED);
      }
      return Account.ofId(card.getAccount().getId());
    }
    if (accountId != null && !StringUtils.isBlank(accountId)) {
      return Account.ofId(accountService.getAccount(userId, accountId).id());
    }
    throw new ApiException(ErrorCode.INVALID_TRANSACTION_PAYLOAD);
  }

  @Override
  public TransactionDetailsResponse execute(TransactionContext transactionContext) {
    final var userId = transactionContext.userId();
    final var user = AppUser.ofId(userId);
    final var accountId = transactionContext.requestDto().accountId();
    final var amount = transactionContext.requestDto().amount();
    final var cardId = transactionContext.requestDto().cardId();

    final var account = resolveAccount(userId, accountId, cardId);

    // Debit Account
    accountService.debitAccount(userId, account.getId().toString(), amount);

    final var transaction = Transaction.builder()
        .appUser(user)
        .account(account)
        .type(transactionContext.requestDto().type())
        .amount(amount)
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
      // Credit Account
      accountService.creditAccount(userId, transaction.getAccount().getId().toString(), transaction.getAmount());
    }

    aiParseTaskService.unlinkTransaction(transaction.getId());
    transactionRepo.delete(transaction);
  }

  @Override
  public Transaction.TransactionType getType() {
    return Transaction.TransactionType.EXPENSE;
  }
}
