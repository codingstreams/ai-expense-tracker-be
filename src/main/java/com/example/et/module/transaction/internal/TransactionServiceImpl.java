package com.example.et.module.transaction.internal;

import com.example.et.core.config.CacheNames;
import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.module.reference.category.SysCategoryService;
import com.example.et.module.reference.category.SystemCategory;
import com.example.et.module.reference.paymentmode.PaymentModeService;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDetailsResponse;
import com.example.et.module.transaction.*;
import com.example.et.module.transaction.dto.CreateTransactionRequest;
import com.example.et.module.transaction.dto.PagedTransactionsDto;
import com.example.et.module.transaction.dto.TransactionDetailsResponse;
import com.example.et.module.transaction.dto.TransactionFilterParams;
import com.example.et.module.transaction.internal.strategy.TransactionStrategyFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
  private final TransactionRepo transactionRepo;
  private final PaymentModeService paymentModeService;
  private final SysCategoryService sysCategoryService;
  private final TransactionStrategyFactory strategyFactory;
  private final TransactionMapper transactionMapper;

  @Override
  @Cacheable(
      value = CacheNames.USER_TRANSACTIONS,
      key = "#userId + ':' + (#filterParams != null ? #filterParams.toCacheKey() : 'all') + ':' + (#pageable.isPaged() ? (#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort) : 'unpaged')",
      unless = "#result == null || #result.content().isEmpty()"
  )
  public PagedTransactionsDto getAllTransactions(String userId, TransactionFilterParams filterParams, Pageable pageable) {
    final var parsedUserId = UUID.fromString(userId);
    final var spec = TransactionSpecification.withFilters(parsedUserId, filterParams);

    final Page<TransactionDetailsResponse> page = transactionRepo.findAll(spec, pageable)
        .map(transactionMapper::toResponseDto);

    return PagedTransactionsDto.from(page);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true),
      @CacheEvict(value = CacheNames.USER_TRANSACTIONS, allEntries = true)
  })
  public TransactionDetailsResponse createTransaction(String userId, CreateTransactionRequest createTransactionRequest) {
    PaymentModeDetailsResponse paymentMode = null;
    SystemCategory category = null;

    if (createTransactionRequest.type() == Transaction.TransactionType.EXPENSE) {
      paymentMode = paymentModeService.getPaymentModeById(UUID.fromString(createTransactionRequest.paymentModeId()));
      category = sysCategoryService.getSystemCategoryById(UUID.fromString(createTransactionRequest.categoryId()));
    }

    final var transactionContext = new TransactionContext(userId, createTransactionRequest, paymentMode, category);
    return strategyFactory.getTransactionStrategy(createTransactionRequest.type())
        .execute(transactionContext);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true),
      @CacheEvict(value = CacheNames.USER_TRANSACTIONS, allEntries = true)
  })
  public void deleteTransaction(String userId, String transactionId) {
    final var transaction = transactionRepo.findByIdAndAppUserId(userId, transactionId)
        .orElseThrow(() -> new ApiException(ErrorCode.TRANSACTION_NOT_FOUND));

    strategyFactory.getTransactionStrategy(transaction.getType())
        .delete(userId, transaction);
  }

  @Override
  @Cacheable(value = CacheNames.USER_TRANSACTIONS, key = "#userId")
  public List<TransactionDetailsResponse> getRecentTransactions(String userId) {
    return getAllTransactions(userId, TransactionFilterParams.empty(), Pageable.ofSize(5)).content();
  }
}
