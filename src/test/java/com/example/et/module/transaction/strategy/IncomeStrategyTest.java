package com.example.et.module.transaction.strategy;

import com.example.et.module.account.AccountService;
import com.example.et.module.ai.parser.AiParseTaskService;
import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.transaction.Transaction;
import com.example.et.module.transaction.TransactionContext;
import com.example.et.module.transaction.TransactionMapper;
import com.example.et.module.transaction.dto.CreateTransactionRequest;
import com.example.et.module.transaction.dto.TransactionDetailsResponse;
import com.example.et.module.transaction.internal.TransactionRepo;
import com.example.et.module.transaction.internal.strategy.IncomeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeStrategyTest {

  @Mock
  private AccountService accountService;

  @Mock
  private TransactionRepo transactionRepo;

  @Mock
  private AiParseTaskService aiParseTaskService;

  @Mock
  private PaymentModeMapper paymentModeMapper;

  @Mock
  private TransactionMapper transactionMapper;

  @InjectMocks
  private IncomeStrategy incomeStrategy;

  private String userId;
  private UUID accountUuid;
  private String accountId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID().toString();
    accountUuid = UUID.randomUUID();
    accountId = accountUuid.toString();
  }

  @Test
  void execute_ShouldCreditAccountAndSaveTransaction() {
    CreateTransactionRequest request = new CreateTransactionRequest(
        null,
        Transaction.TransactionType.INCOME,
        3000.0f,
        LocalDate.now(),
        "Bonus",
        accountId,
        null,
        null,
        null,
        null,
        null
    );

    TransactionContext context = new TransactionContext(userId, request, null, null);
    Transaction savedTxn = Transaction.builder().id(UUID.randomUUID()).amount(3000.0f).build();
    TransactionDetailsResponse expectedResponse = new TransactionDetailsResponse(
        savedTxn.getId(), Transaction.TransactionType.INCOME, 3000.0f, LocalDate.now(), "Bonus", "Acc", "Bank", null
    );

    doNothing().when(accountService).creditAccount(userId, accountId, 3000.0f);
    when(transactionRepo.save(any(Transaction.class))).thenReturn(savedTxn);
    when(transactionMapper.toResponseDto(savedTxn)).thenReturn(expectedResponse);

    TransactionDetailsResponse actual = incomeStrategy.execute(context);

    assertNotNull(actual);
    assertEquals(expectedResponse, actual);

    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepo, times(1)).save(captor.capture());
    Transaction captured = captor.getValue();
    assertEquals(3000.0f, captured.getAmount());
    assertEquals(Transaction.TransactionType.INCOME, captured.getType());
    assertEquals(accountUuid, captured.getAccount().getId());

    verify(accountService, times(1)).creditAccount(userId, accountId, 3000.0f);
  }

  @Test
  void delete_ShouldDebitAccountUnlinkAiTaskAndDelete() {
    UUID txId = UUID.randomUUID();
    com.example.et.module.account.Account account = com.example.et.module.account.Account.builder().id(accountUuid).build();
    Transaction transaction = Transaction.builder()
        .id(txId)
        .account(account)
        .amount(2000.0f)
        .build();

    doNothing().when(accountService).debitAccount(userId, accountUuid.toString(), 2000.0f);
    doNothing().when(aiParseTaskService).unlinkTransaction(txId);
    doNothing().when(transactionRepo).delete(transaction);

    incomeStrategy.delete(userId, transaction);

    verify(accountService, times(1)).debitAccount(userId, accountUuid.toString(), 2000.0f);
    verify(aiParseTaskService, times(1)).unlinkTransaction(txId);
    verify(transactionRepo, times(1)).delete(transaction);
  }

  @Test
  void delete_ShouldNotDebitAccount_WhenAccountIsNull() {
    UUID txId = UUID.randomUUID();
    Transaction transaction = Transaction.builder()
        .id(txId)
        .account(null)
        .amount(2000.0f)
        .build();

    doNothing().when(aiParseTaskService).unlinkTransaction(txId);
    doNothing().when(transactionRepo).delete(transaction);

    incomeStrategy.delete(userId, transaction);

    verify(accountService, never()).debitAccount(any(), any(), anyFloat());
    verify(aiParseTaskService, times(1)).unlinkTransaction(txId);
    verify(transactionRepo, times(1)).delete(transaction);
  }

  @Test
  void getType_ShouldReturnIncome() {
    assertEquals(Transaction.TransactionType.INCOME, incomeStrategy.getType());
  }
}
