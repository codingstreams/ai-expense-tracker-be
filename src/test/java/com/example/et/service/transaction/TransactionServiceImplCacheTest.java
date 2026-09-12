package com.example.et.service.transaction;

import com.example.et.config.CacheConfig;
import com.example.et.controller.dto.transaction.TransactionFilterParams;
import com.example.et.controller.dto.transaction.TransactionResponseDto;
import com.example.et.mapper.TransactionMapper;
import com.example.et.model.core.Transaction;
import com.example.et.repo.TransactionRepo;
import com.example.et.service.category.SysCategoryService;
import com.example.et.service.paymentmode.PaymentModeService;
import com.example.et.service.transaction.strategy.TransactionStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    TransactionServiceImpl.class,
    TransactionServiceImplCacheTest.TestCacheConfig.class
})
class TransactionServiceImplCacheTest {

  @MockitoBean
  private TransactionRepo transactionRepo;
  @MockitoBean
  private TransactionMapper transactionMapper;
  @MockitoBean
  private PaymentModeService paymentModeService;
  @MockitoBean
  private SysCategoryService sysCategoryService;
  @MockitoBean
  private TransactionStrategyFactory strategyFactory;
  @Autowired
  private TransactionService transactionService;
  @Autowired
  private CacheManager cacheManager;

  @Test
  void getAllTransactions_ShouldCacheResult_WhenListIsNotEmpty() {
    String userId = UUID.randomUUID().toString();
    TransactionFilterParams filterParams = TransactionFilterParams.empty();
    Pageable pageable = PageRequest.of(0, 10);

    Transaction transaction = new Transaction();
    TransactionResponseDto responseDto = new TransactionResponseDto(
        UUID.randomUUID(),
        Transaction.TransactionType.EXPENSE,
        100.0f,
        LocalDate.now(),
        "Test description",
        "Account",
        "UPI",
        "Food"
    );
    Page<Transaction> page = new PageImpl<>(List.of(transaction), pageable, 1);

    when(transactionRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
    when(transactionMapper.toResponseDto(any())).thenReturn(responseDto);

    // First call (populates cache)
    var firstCall = transactionService.getAllTransactions(userId, filterParams, pageable);
    assertNotNull(firstCall);
    assertEquals(1, firstCall.content().size());

    // Second call (hits cache)
    var secondCall = transactionService.getAllTransactions(userId, filterParams, pageable);
    assertNotNull(secondCall);
    assertEquals(1, secondCall.content().size());

    // Repo should only be queried once due to caching
    verify(transactionRepo, times(1)).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  void getAllTransactions_ShouldNotCacheResult_WhenListIsEmpty() {
    String userId = UUID.randomUUID().toString();
    TransactionFilterParams filterParams = TransactionFilterParams.empty();
    Pageable pageable = PageRequest.of(0, 10);

    Page<Transaction> emptyPage = Page.empty(pageable);

    when(transactionRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

    // First call (empty list, not cached due to unless condition)
    transactionService.getAllTransactions(userId, filterParams, pageable);

    // Second call (hits repo again)
    transactionService.getAllTransactions(userId, filterParams, pageable);

    // Repo should be queried twice because empty results are not cached
    verify(transactionRepo, times(2)).findAll(any(Specification.class), eq(pageable));
  }

  @Configuration
  @EnableCaching
  static class TestCacheConfig {
    @Bean
    public CacheManager cacheManager() {
      return new ConcurrentMapCacheManager(CacheConfig.USER_TRANSACTIONS_CACHE);
    }
  }
}