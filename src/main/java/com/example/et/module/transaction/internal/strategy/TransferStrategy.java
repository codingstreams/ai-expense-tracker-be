package com.example.et.module.transaction.internal.strategy;

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

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransferStrategy implements TransactionStrategy {
  private final AccountService accountService;
  private final TransactionRepo transactionRepo;
  private final AiParseTaskService aiParseTaskService;
  private final PaymentModeMapper paymentModeMapper;
  private final TransactionMapper transactionMapper;

  @Override
  public TransactionResponseDto execute(TransactionContext transactionContext) {
    final var userId = transactionContext.userId();
    final var user = AppUser.ofId(userId);

    final var sourceAccount = accountService.getAccount(UUID.fromString(userId), transactionContext.requestDto().accountId());
    final var destAccount = accountService.getAccount(UUID.fromString(userId), transactionContext.requestDto().toAccountId());

    final var transferId = UUID.randomUUID();

    sourceAccount.debit(transactionContext.requestDto().amount());
    destAccount.credit(transactionContext.requestDto().amount());

    accountService.saveAccount(sourceAccount);
    accountService.saveAccount(destAccount);

    final var debitTxn = Transaction.builder()
        .appUser(user)
        .account(sourceAccount)
        .type(Transaction.TransactionType.TRANSFER)
        .amount(-transactionContext.requestDto().amount())
        .transactionDate(transactionContext.requestDto().transactionDate())
        .description(transactionContext.requestDto().description())
        .paymentMode(paymentModeMapper.toEntity(transactionContext.paymentMode()))
        .transferId(transferId)
        .build();

    final var creditTxn = Transaction.builder()
        .appUser(user)
        .account(destAccount)
        .type(Transaction.TransactionType.TRANSFER)
        .amount(transactionContext.requestDto().amount())
        .transactionDate(transactionContext.requestDto().transactionDate())
        .description(transactionContext.requestDto().description())
        .paymentMode(paymentModeMapper.toEntity(transactionContext.paymentMode()))
        .transferId(transferId)
        .build();

    transactionRepo.save(debitTxn);
    return transactionMapper.toResponseDto(transactionRepo.save(creditTxn));
  }

  @Override
  public void delete(String userId, Transaction transaction) {
    final var transferTxns = transactionRepo.findAllByTransferIdAndAppUserId(transaction.getTransferId(), UUID.fromString(userId));

    for (var txn : transferTxns) {
      if (txn.getAccount() != null) {
        final var acc = txn.getAccount();
        if (txn.getAmount() < 0) {
          acc.credit(-txn.getAmount());
        } else {
          acc.debit(txn.getAmount());
        }
        accountService.saveAccount(acc);
      }
      aiParseTaskService.unlinkTransaction(txn.getId());
    }
    transactionRepo.deleteAll(transferTxns);
  }

  @Override
  public Transaction.TransactionType getType() {
    return Transaction.TransactionType.TRANSFER;
  }
}
