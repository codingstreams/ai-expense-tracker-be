package com.example.et.service.transaction;

import com.example.et.config.CacheConfig;
import com.example.et.controller.dto.paymentmode.PaymentModeDto;
import com.example.et.controller.dto.transaction.PagedTransactionsDto;
import com.example.et.controller.dto.transaction.TransactionFilterParams;
import com.example.et.controller.dto.transaction.TransactionRequestDto;
import com.example.et.controller.dto.transaction.TransactionResponseDto;
import com.example.et.mapper.TransactionMapper;
import com.example.et.model.core.SystemCategory;
import com.example.et.model.core.Transaction;
import com.example.et.repo.TransactionRepo;
import com.example.et.repo.spec.TransactionSpecification;
import com.example.et.service.category.SysCategoryService;
import com.example.et.service.paymentmode.PaymentModeService;
import com.example.et.service.transaction.strategy.TransactionStrategyFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
      value = CacheConfig.USER_TRANSACTIONS_CACHE,
      key = "{#userId, #filterParams, #pageable.getPageNumber(), #pageable.getPageSize()}",
      unless = "#result == null || #result.content().isEmpty()"
  )
  public PagedTransactionsDto getAllTransactions(String userId, TransactionFilterParams filterParams, Pageable pageable) {
    final var parsedUserId = UUID.fromString(userId);
    final var spec = TransactionSpecification.withFilters(parsedUserId, filterParams);

    final var page = transactionRepo.findAll(spec, pageable)
        .map(transactionMapper::toResponseDto);

    return PagedTransactionsDto.from(page);
  }

  @Override
  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CacheConfig.USER_BANK_ACCOUNTS_CACHE, key = "#userId"),
      @CacheEvict(value = CacheConfig.USER_BANK_ACCOUNTS_CACHE, key = "{#userId, #accountId}"),
      @CacheEvict(value = CacheConfig.USER_BANK_ACCOUNTS_CACHE, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheConfig.USER_FINANCIAL_SUMMARY_CACHE, allEntries = true),
      @CacheEvict(value = CacheConfig.USER_TRANSACTIONS_CACHE, key = "#userId"),
      @CacheEvict(
          value = CacheConfig.USER_TRANSACTIONS_CACHE,
          key = "{#userId, #filterParams, #pageable.getPageNumber(), #pageable.getPageSize()}"
      )
  })
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
  @Caching(evict = {
      @CacheEvict(value = CacheConfig.USER_BANK_ACCOUNTS_CACHE, key = "#userId"),
      @CacheEvict(value = CacheConfig.USER_BANK_ACCOUNTS_CACHE, key = "{#userId, #accountId}"),
      @CacheEvict(value = CacheConfig.USER_BANK_ACCOUNTS_CACHE, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheConfig.USER_FINANCIAL_SUMMARY_CACHE, allEntries = true),
      @CacheEvict(value = CacheConfig.USER_TRANSACTIONS_CACHE, key = "#userId"),
      @CacheEvict(
          value = CacheConfig.USER_TRANSACTIONS_CACHE,
          key = "{#userId, #filterParams, #pageable.getPageNumber(), #pageable.getPageSize()}"
      )
  })
  public void deleteTransaction(String userId, UUID transactionId) {
    final var userUuid = UUID.fromString(userId);
    final var transaction = transactionRepo.findByIdAndAppUserId(transactionId, userUuid)
        .orElseThrow(() -> new RuntimeException("Transaction not found"));

    strategyFactory.getTransactionStrategy(transaction.getType())
        .delete(userId, transaction);
  }

  @Override
  @Cacheable(value = CacheConfig.USER_TRANSACTIONS_CACHE, key = "#userId")
  public List<TransactionResponseDto> getRecentTransactions(String userId) {
    return getAllTransactions(userId, TransactionFilterParams.empty(), Pageable.ofSize(5)).content();
  }
}
