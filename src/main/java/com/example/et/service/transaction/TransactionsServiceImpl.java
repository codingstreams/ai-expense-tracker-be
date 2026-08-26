package com.example.et.service.transaction;

import com.example.et.controller.dto.PagedTransactionsDto;
import com.example.et.controller.dto.TransactionDto;
import com.example.et.controller.dto.TransactionRequestDto;
import com.example.et.controller.dto.TransactionResponseDto;
import com.example.et.model.core.*;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.repo.SystemCategoryRepo;
import com.example.et.repo.TransactionRepo;
import com.example.et.service.account.AccountService;
import com.example.et.service.ai.parsetask.AiParseTaskService;
import com.example.et.service.card.CardService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionsServiceImpl implements TransactionsService {
  private final TransactionRepo transactionRepo;
  private final AccountService accountService;
  private final CardService cardService;
  private final PaymentModeRepo paymentModeRepo;
  private final SystemCategoryRepo systemCategoryRepo;
  private final AiParseTaskService aiParseTaskService;

  private TransactionDto toDto(Transaction t) {
    return new TransactionDto(
        t.getId(),
        t.getType(),
        t.getAmount(),
        t.getTransactionDate(),
        t.getDescription(),
        t.getAccount() != null ? t.getAccount().getId() : null,
        t.getPaymentMode() != null ? t.getPaymentMode().getId() : null,
        t.getTransactionCategory() != null ? t.getTransactionCategory().getId() : null,
        t.getTransferId()
    );
  }

  private Account resolveAccount(String userId, UUID accountId, UUID cardId) {
    if (cardId != null) {
      final var card = cardService.getUserCard(userId, cardId);
      if (card.getAccount() == null) {
        throw new RuntimeException("Card is not linked to any account");
      }
      return card.getAccount();
    }
    if (accountId != null) {
      return accountService.getUserAccount(userId, accountId);
    }
    throw new RuntimeException("Either accountId or cardId must be provided");
  }

  @Override
  @Transactional
  public TransactionDto createTransaction(String userId, TransactionRequestDto dto) {
    final var user = AppUser.ofId(userId);
    final var paymentMode = dto.paymentModeId() != null ? paymentModeRepo.findById(dto.paymentModeId()).orElse(null) : null;
    final var category = dto.categoryId() != null ? systemCategoryRepo.findById(dto.categoryId()).orElse(null) : null;
    final var date = dto.transactionDate() != null ? dto.transactionDate() : LocalDate.now();

    if (dto.type() == Transaction.TransactionType.TRANSFER) {
      final var fromAccount = resolveAccount(userId, dto.accountId(), dto.cardId());
      final var toAccount = accountService.getUserAccount(userId, dto.toAccountId());

      final var transferId = UUID.randomUUID();

      fromAccount.setBalance(fromAccount.getBalance() - dto.amount());
      toAccount.setBalance(toAccount.getBalance() + dto.amount());
      accountService.saveAccount(fromAccount);
      accountService.saveAccount(toAccount);

      final var debitTxn = Transaction.builder()
          .appUser(user)
          .account(fromAccount)
          .type(Transaction.TransactionType.TRANSFER)
          .amount(-dto.amount())
          .transactionDate(date)
          .description(dto.description())
          .paymentMode(paymentMode)
          .transferId(transferId)
          .build();

      final var creditTxn = Transaction.builder()
          .appUser(user)
          .account(toAccount)
          .type(Transaction.TransactionType.TRANSFER)
          .amount(dto.amount())
          .transactionDate(date)
          .description(dto.description())
          .paymentMode(paymentMode)
          .transferId(transferId)
          .build();

      transactionRepo.save(debitTxn);
      return toDto(transactionRepo.save(creditTxn));
    }

    final var account = resolveAccount(userId, dto.accountId(), dto.cardId());

    if (dto.type() == Transaction.TransactionType.EXPENSE) {
      account.setBalance(account.getBalance() - dto.amount());
    } else {
      account.setBalance(account.getBalance() + dto.amount());
    }
    accountService.saveAccount(account);

    final var transaction = Transaction.builder()
        .appUser(user)
        .account(account)
        .type(dto.type())
        .amount(dto.type() == Transaction.TransactionType.EXPENSE ? -dto.amount() : dto.amount())
        .transactionDate(date)
        .description(dto.description())
        .paymentMode(paymentMode)
        .transactionCategory(category)
        .build();

    return toDto(transactionRepo.save(transaction));
  }

  @Override
  public PagedTransactionsDto getAllTransactions(String userId, Pageable pageable) {
    final var page = transactionRepo.findByAppUserId(UUID.fromString(userId), pageable).map(TransactionsServiceImpl::toDtoV2);
    return PagedTransactionsDto.from(page);
  }

  @Override
  public List<TransactionDto> getRecentTransactions(String userId) {
    return transactionRepo.findByAppUserIdOrderByTransactionDateDesc(UUID.fromString(userId), PageRequest.of(0, 5))
        .stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  public List<TransactionResponseDto> getRecentTransactionsV2(String userId) {
    return transactionRepo.findByAppUserIdOrderByTransactionDateDesc(UUID.fromString(userId), PageRequest.of(0, 5))
        .stream()
        .map(TransactionsServiceImpl::toDtoV2)
        .toList();
  }

  private static @NonNull TransactionResponseDto toDtoV2(Transaction t) {
    return new TransactionResponseDto(
            t.getId(),
            t.getType(),
            t.getAmount(),
            t.getTransactionDate(),
            t.getDescription(),
            Optional.ofNullable(t.getAccount())
                    .map(Account::getBank)
                    .map(Bank::getName)
                    .orElse("CASH"),
            Optional.ofNullable(t.getPaymentMode())
                    .map(PaymentMode::getName)
                    .orElse(""),
            Optional.ofNullable(t.getTransactionCategory())
                    .map(SystemCategory::getName)
                    .orElse("")
    );
  }

  @Override
  @Transactional
  public void deleteTransaction(String userId, UUID transactionId) {
    final var userUuid = UUID.fromString(userId);
    final var transaction = transactionRepo.findByIdAndAppUserId(transactionId, userUuid)
        .orElseThrow(() -> new RuntimeException("Transaction not found"));

    if (transaction.getType() == Transaction.TransactionType.TRANSFER && transaction.getTransferId() != null) {
      final var transferTxns = transactionRepo.findAllByTransferIdAndAppUserId(transaction.getTransferId(), userUuid);
      for (var txn : transferTxns) {
        if (txn.getAccount() != null) {
          final var acc = txn.getAccount();
          acc.setBalance(acc.getBalance() - txn.getAmount());
          accountService.saveAccount(acc);
        }
        aiParseTaskService.unlinkTransaction(txn.getId());
      }
      transactionRepo.deleteAll(transferTxns);
      return;
    }

    if (transaction.getAccount() != null) {
      final var account = transaction.getAccount();
      account.setBalance(account.getBalance() - transaction.getAmount());
      accountService.saveAccount(account);
    }

    aiParseTaskService.unlinkTransaction(transactionId);
    transactionRepo.delete(transaction);
  }
}
