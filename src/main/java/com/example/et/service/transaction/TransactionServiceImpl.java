package com.example.et.service.transaction;

import com.example.et.controller.dto.PagedTransactionsDto;
import com.example.et.controller.dto.TransactionFilterParams;
import com.example.et.controller.dto.TransactionRequestDto;
import com.example.et.controller.dto.TransactionResponseDto;
import com.example.et.model.core.*;
import com.example.et.repo.PaymentModeRepo;
import com.example.et.repo.SysCategoryRepo;
import com.example.et.repo.TransactionRepo;
import com.example.et.repo.spec.TransactionSpecification;
import com.example.et.service.account.AccountService;
import com.example.et.service.card.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
  private final TransactionRepo transactionRepo;
  private final PaymentModeRepo paymentModeRepo;
  private final SysCategoryRepo sysCategoryRepo;
  private final AccountService accountService;
  private final CardService cardService;

  private static TransactionResponseDto toDto(Transaction t) {
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
  public PagedTransactionsDto getAllTransactions(String userId, TransactionFilterParams filterParams, Pageable pageable) {
    final var parsedUserId = UUID.fromString(userId);
    final var spec = TransactionSpecification.withFilters(parsedUserId, filterParams);

    final var page = transactionRepo.findAll(spec, pageable)
        .map(TransactionServiceImpl::toDto);

    return PagedTransactionsDto.from(page);
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
      return accountService.getAccount(UUID.fromString(userId), accountId);
    }
    throw new RuntimeException("Either accountId or cardId must be provided");
  }

  @Override
  public TransactionResponseDto createTransaction(String userId, TransactionRequestDto requestBody) {
    final var user = AppUser.ofId(userId);
    final var paymentMode = requestBody.paymentModeId() != null ? paymentModeRepo.findById(requestBody.paymentModeId()).orElse(null) : null;
    final var category = requestBody.categoryId() != null ? sysCategoryRepo.findById(requestBody.categoryId()).orElse(null) : null;
    final var date = requestBody.transactionDate() != null ? requestBody.transactionDate() : LocalDate.now();

    final var account = resolveAccount(userId, requestBody.accountId(), requestBody.cardId());

    Transaction savedTransaction = null;

    switch (requestBody.type()) {
      case EXPENSE -> {
        account.setBalance(account.getBalance() - requestBody.amount());
        accountService.saveAccount(account);

        final var transaction = Transaction.builder()
            .appUser(user)
            .account(account)
            .type(requestBody.type())
            .amount(-requestBody.amount())
            .transactionDate(date)
            .description(requestBody.description())
            .paymentMode(paymentMode)
            .transactionCategory(category)
            .build();

        savedTransaction = transactionRepo.save(transaction);
      }
      case INCOME ->{
        account.setBalance(account.getBalance() + requestBody.amount());
        accountService.saveAccount(account);

        final var transaction = Transaction.builder()
            .appUser(user)
            .account(account)
            .type(requestBody.type())
            .amount(requestBody.amount())
            .transactionDate(date)
            .description(requestBody.description())
            .paymentMode(paymentMode)
            .transactionCategory(category)
            .build();

        savedTransaction = transactionRepo.save(transaction);
      }
      case TRANSFER ->{
        final var sourceAccount = accountService.getAccount(requestBody.accountId(), UUID.fromString(userId));
        final var destAccount = accountService.getAccount(requestBody.toAccountId(), UUID.fromString(userId));

        final var transferId = UUID.randomUUID();

        sourceAccount.setBalance(sourceAccount.getBalance() - requestBody.amount());
        destAccount.setBalance(destAccount.getBalance() + requestBody.amount());
        accountService.saveAccount(sourceAccount);
        accountService.saveAccount(destAccount);

        final var debitTxn = Transaction.builder()
            .appUser(user)
            .account(sourceAccount)
            .type(Transaction.TransactionType.TRANSFER)
            .amount(-requestBody.amount())
            .transactionDate(date)
            .description(requestBody.description())
            .paymentMode(paymentMode)
            .transferId(transferId)
            .build();

        final var creditTxn = Transaction.builder()
            .appUser(user)
            .account(destAccount)
            .type(Transaction.TransactionType.TRANSFER)
            .amount(requestBody.amount())
            .transactionDate(date)
            .description(requestBody.description())
            .paymentMode(paymentMode)
            .transferId(transferId)
            .build();

        transactionRepo.save(debitTxn);
        savedTransaction = transactionRepo.save(creditTxn);
      }
    }

    return toDto(savedTransaction);
  }
}
