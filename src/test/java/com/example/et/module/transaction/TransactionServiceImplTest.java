package com.example.et.module.transaction;

import com.example.et.module.reference.category.SysCategoryService;
import com.example.et.module.reference.category.SystemCategory;
import com.example.et.module.reference.paymentmode.PaymentModeService;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDetailsResponse;
import com.example.et.module.transaction.dto.PagedTransactionsDto;
import com.example.et.module.transaction.dto.TransactionFilterParams;
import com.example.et.module.transaction.dto.TransactionRequestDto;
import com.example.et.module.transaction.dto.TransactionResponseDto;
import com.example.et.module.transaction.internal.TransactionRepo;
import com.example.et.module.transaction.internal.TransactionServiceImpl;
import com.example.et.module.transaction.internal.strategy.TransactionStrategy;
import com.example.et.module.transaction.internal.strategy.TransactionStrategyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.Optional;
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

    PagedTransactionsDto result = transactionService.getAllTransactions(userId, filterParams, pageable);

    assertNotNull(result);
    assertEquals(1, result.content().size());
    assertEquals(responseDto, result.content().get(0));

    verify(transactionRepo).findAll(any(Specification.class), eq(pageable));
    verify(transactionMapper).toResponseDto(transaction);
  }

  @Test
  void getAllTransactions_ShouldReturnEmptyPagedTransactions_WhenNoTransactionsExist() {
    String userId = UUID.randomUUID().toString();
    TransactionFilterParams filterParams = TransactionFilterParams.empty();
    Pageable pageable = PageRequest.of(0, 10);

    Page<Transaction> emptyPage = Page.empty(pageable);

    when(transactionRepo.findAll(any(Specification.class), eq(pageable)))
        .thenReturn(emptyPage);

    PagedTransactionsDto result = transactionService.getAllTransactions(userId, filterParams, pageable);

    assertNotNull(result);
    assertTrue(result.content().isEmpty());

    verify(transactionRepo).findAll(any(Specification.class), eq(pageable));
    verifyNoInteractions(transactionMapper);
  }

  @Test
  void createTransaction_ShouldDelegateToExpenseStrategy_WhenTypeIsExpense() {
    String userId = UUID.randomUUID().toString();
    UUID paymentModeId = UUID.randomUUID();
    UUID categoryId = UUID.randomUUID();

    TransactionRequestDto request = new TransactionRequestDto(
        null,
        Transaction.TransactionType.EXPENSE,
        250.0f,
        LocalDate.now(),
        "Grocery store",
        UUID.randomUUID().toString(),
        null,
        null,
        paymentModeId.toString(),
        categoryId.toString(),
        null
    );

    PaymentModeDetailsResponse paymentMode = new PaymentModeDetailsResponse(paymentModeId, "UPI");
    SystemCategory category = SystemCategory.builder().id(categoryId).name("Groceries").build();
    TransactionStrategy mockStrategy = mock(TransactionStrategy.class);
    TransactionResponseDto expectedResponse = new TransactionResponseDto(
        UUID.randomUUID(), Transaction.TransactionType.EXPENSE, 250.0f, LocalDate.now(), "Grocery store", "Acc", "UPI", "Groceries"
    );

    when(paymentModeService.getPaymentModeById(paymentModeId)).thenReturn(paymentMode);
    when(sysCategoryService.getSystemCategoryById(categoryId)).thenReturn(category);
    when(strategyFactory.getTransactionStrategy(Transaction.TransactionType.EXPENSE)).thenReturn(mockStrategy);
    when(mockStrategy.execute(any(TransactionContext.class))).thenReturn(expectedResponse);

    TransactionResponseDto actualResponse = transactionService.createTransaction(userId, request);

    assertNotNull(actualResponse);
    assertEquals(expectedResponse, actualResponse);

    ArgumentCaptor<TransactionContext> contextCaptor = ArgumentCaptor.forClass(TransactionContext.class);
    verify(mockStrategy, times(1)).execute(contextCaptor.capture());
    TransactionContext context = contextCaptor.getValue();
    assertEquals(userId, context.userId());
    assertEquals(paymentMode, context.paymentMode());
    assertEquals(category, context.systemCategory());
  }

  @Test
  void createTransaction_ShouldDelegateToIncomeStrategy_WhenTypeIsIncome() {
    String userId = UUID.randomUUID().toString();

    TransactionRequestDto request = new TransactionRequestDto(
        null,
        Transaction.TransactionType.INCOME,
        5000.0f,
        LocalDate.now(),
        "Salary",
        UUID.randomUUID().toString(),
        null,
        null,
        null,
        null,
        null
    );

    TransactionStrategy mockStrategy = mock(TransactionStrategy.class);
    TransactionResponseDto expectedResponse = new TransactionResponseDto(
        UUID.randomUUID(), Transaction.TransactionType.INCOME, 5000.0f, LocalDate.now(), "Salary", "Acc", "Bank", "Salary"
    );

    when(strategyFactory.getTransactionStrategy(Transaction.TransactionType.INCOME)).thenReturn(mockStrategy);
    when(mockStrategy.execute(any(TransactionContext.class))).thenReturn(expectedResponse);

    TransactionResponseDto actualResponse = transactionService.createTransaction(userId, request);

    assertNotNull(actualResponse);
    assertEquals(expectedResponse, actualResponse);

    verifyNoInteractions(paymentModeService);
    verifyNoInteractions(sysCategoryService);
  }

  @Test
  void deleteTransaction_ShouldFindAndDelegateToDelete() {
    String userId = UUID.randomUUID().toString();
    UUID userUuid = UUID.fromString(userId);
    UUID transactionId = UUID.randomUUID();

    Transaction transaction = Transaction.builder()
        .id(transactionId)
        .type(Transaction.TransactionType.EXPENSE)
        .amount(100.0f)
        .build();

    TransactionStrategy mockStrategy = mock(TransactionStrategy.class);

    when(transactionRepo.findByIdAndAppUserId(transactionId, userUuid)).thenReturn(Optional.of(transaction));
    when(strategyFactory.getTransactionStrategy(Transaction.TransactionType.EXPENSE)).thenReturn(mockStrategy);

    transactionService.deleteTransaction(userId, transactionId);

    verify(mockStrategy, times(1)).delete(userId, transaction);
  }

  @Test
  void deleteTransaction_ShouldThrowException_WhenTransactionNotFound() {
    String userId = UUID.randomUUID().toString();
    UUID userUuid = UUID.fromString(userId);
    UUID transactionId = UUID.randomUUID();

    when(transactionRepo.findByIdAndAppUserId(transactionId, userUuid)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> transactionService.deleteTransaction(userId, transactionId));
    verifyNoInteractions(strategyFactory);
  }

  @Test
  void getRecentTransactions_ShouldReturnFirstFiveTransactions() {
    String userId = UUID.randomUUID().toString();

    Transaction transaction = new Transaction();
    TransactionResponseDto responseDto = new TransactionResponseDto(
        UUID.randomUUID(), Transaction.TransactionType.EXPENSE, 100.0f, LocalDate.now(), "Desc", "Acc", "UPI", "Food"
    );

    Page<Transaction> page = new PageImpl<>(List.of(transaction), PageRequest.of(0, 5), 1);
    when(transactionRepo.findAll(any(Specification.class), eq(Pageable.ofSize(5)))).thenReturn(page);
    when(transactionMapper.toResponseDto(transaction)).thenReturn(responseDto);

    List<TransactionResponseDto> recents = transactionService.getRecentTransactions(userId);

    assertNotNull(recents);
    assertEquals(1, recents.size());
  }
}