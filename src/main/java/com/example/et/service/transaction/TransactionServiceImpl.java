package com.example.et.service.transaction;

import com.example.et.controller.dto.paymentmode.PaymentModeDto;
import com.example.et.controller.dto.transaction.PagedTransactionsDto;
import com.example.et.controller.dto.transaction.TransactionFilterParams;
import com.example.et.controller.dto.transaction.TransactionRequestDto;
import com.example.et.controller.dto.transaction.TransactionResponseDto;
import com.example.et.model.core.*;
import com.example.et.repo.TransactionRepo;
import com.example.et.repo.spec.TransactionSpecification;
import com.example.et.service.category.SysCategoryService;
import com.example.et.service.paymentmode.PaymentModeService;
import com.example.et.service.transaction.strategy.TransactionStrategyFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
  private final TransactionRepo transactionRepo;
  private final PaymentModeService paymentModeService;
  private final SysCategoryService sysCategoryService;
  private final TransactionStrategyFactory strategyFactory;

  public static TransactionResponseDto toDto(Transaction t) {
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

  @Override
  @Transactional
//  @CacheEvict()
  public TransactionResponseDto createTransaction(String userId, TransactionRequestDto requestBody) {
    PaymentModeDto paymentMode = null;
    SystemCategory category = null;

    if (requestBody.type() == Transaction.TransactionType.EXPENSE) {
      paymentMode = paymentModeService.getPaymentModeById(requestBody.paymentModeId());
      category = sysCategoryService.getSystemCategoryById(requestBody.categoryId());
    }

    final var transactionContext = new TransactionContext(userId, requestBody, paymentMode, category);
    return strategyFactory.getTransactionStrategy(requestBody.type())
        .execute(transactionContext);
  }

  @Override
  @Transactional
  public void deleteTransaction(String userId, UUID transactionId) {
    final var userUuid = UUID.fromString(userId);
    final var transaction = transactionRepo.findByIdAndAppUserId(transactionId, userUuid)
        .orElseThrow(() -> new RuntimeException("Transaction not found"));

    strategyFactory.getTransactionStrategy(transaction.getType())
        .delete(userId, transaction);
  }

  @Override
  public List<TransactionResponseDto> getRecentTransactions(String userId) {
    return getAllTransactions(userId, TransactionFilterParams.empty(), Pageable.ofSize(5)).content();
  }
}
