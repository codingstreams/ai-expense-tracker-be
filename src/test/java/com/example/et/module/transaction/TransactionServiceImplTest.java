package com.example.et.module.transaction;

import com.example.et.module.reference.category.SysCategoryService;
import com.example.et.module.reference.paymentmode.PaymentModeService;
import com.example.et.module.transaction.dto.PagedTransactionsDto;
import com.example.et.module.transaction.dto.TransactionFilterParams;
import com.example.et.module.transaction.dto.TransactionResponseDto;
import com.example.et.module.transaction.internal.TransactionRepo;
import com.example.et.module.transaction.internal.TransactionServiceImpl;
import com.example.et.module.transaction.internal.strategy.TransactionStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

  @Mock
  private TransactionRepo transactionRepo;

  @Mock
  private TransactionMapper transactionMapper;

  // Injected mock placeholders required by @RequiredArgsConstructor
  @Mock
  private PaymentModeService paymentModeService;
  @Mock
  private SysCategoryService sysCategoryService;
  @Mock
  private TransactionStrategyFactory strategyFactory;

  @InjectMocks
  private TransactionServiceImpl transactionService;

  @Test
  void getAllTransactions_ShouldReturnPagedTransactions_WhenTransactionsExist() {
    // Given
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

    Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

    when(transactionRepo.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(transactionPage);
    when(transactionMapper.toResponseDto(transaction))
        .thenReturn(responseDto);

    // When
    PagedTransactionsDto result = transactionService.getAllTransactions(userId, filterParams, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.content().size());
    assertEquals(responseDto, result.content().get(0));

    verify(transactionRepo).findAll(any(Specification.class), eq(pageable));
    verify(transactionMapper).toResponseDto(transaction);
  }

  @Test
  void getAllTransactions_ShouldReturnEmptyPagedTransactions_WhenNoTransactionsExist() {
    // Given
    String userId = UUID.randomUUID().toString();
    TransactionFilterParams filterParams = TransactionFilterParams.empty();
    Pageable pageable = PageRequest.of(0, 10);

    Page<Transaction> emptyPage = Page.empty(pageable);

    when(transactionRepo.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(emptyPage);

    // When
    PagedTransactionsDto result = transactionService.getAllTransactions(userId, filterParams, pageable);

    // Then
    assertNotNull(result);
    assertTrue(result.content().isEmpty());

    verify(transactionRepo).findAll(any(Specification.class), eq(pageable));
    verifyNoInteractions(transactionMapper);
  }
}