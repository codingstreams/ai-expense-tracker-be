package com.example.et.module.transaction.strategy;

import com.example.et.module.account.Account;
import com.example.et.module.account.AccountService;
import com.example.et.module.ai.parser.AiParseTaskService;
import com.example.et.module.reference.paymentmode.PaymentMode;
import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDetailsResponse;
import com.example.et.module.transaction.Transaction;
import com.example.et.module.transaction.TransactionContext;
import com.example.et.module.transaction.TransactionMapper;
import com.example.et.module.transaction.dto.CreateTransactionRequest;
import com.example.et.module.transaction.dto.TransactionDetailsResponse;
import com.example.et.module.transaction.internal.TransactionRepo;
import com.example.et.module.transaction.internal.strategy.TransferStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferStrategyTest {

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
  private TransferStrategy transferStrategy;

  private String userId;
  private UUID sourceAccountUuid;
  private String sourceAccountId;
  private UUID destAccountUuid;
  private String destAccountId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID().toString();
    sourceAccountUuid = UUID.randomUUID();
    sourceAccountId = sourceAccountUuid.toString();
    destAccountUuid = UUID.randomUUID();
    destAccountId = destAccountUuid.toString();
  }

  @Test
  void execute_ShouldDebitSourceAndCreditDestinationAndSaveBothTransactions() {
    LocalDate txnDate = LocalDate.now();
    CreateTransactionRequest request = new CreateTransactionRequest(
        null,
        Transaction.TransactionType.TRANSFER,
        500.0f,
        txnDate,
        "Account transfer",
        sourceAccountId,
        null,
        destAccountId,
        null,
        null,
        null
    );

    PaymentModeDetailsResponse paymentModeDto = new PaymentModeDetailsResponse(UUID.randomUUID(), "NET_BANKING");
    PaymentMode paymentModeEntity = PaymentMode.builder().id(paymentModeDto.id()).name("NET_BANKING").build();
    TransactionContext context = new TransactionContext(userId, request, paymentModeDto, null);

    Transaction creditTxn = Transaction.builder().id(UUID.randomUUID()).amount(500.0f).build();
    TransactionDetailsResponse expectedResponse = new TransactionDetailsResponse(
        creditTxn.getId(), Transaction.TransactionType.TRANSFER, 500.0f, txnDate, "Account transfer", "Acc", "NET_BANKING", null
    );

    doNothing().when(accountService).debitAccount(userId, sourceAccountId, 500.0f);
    doNothing().when(accountService).creditAccount(userId, destAccountId, 500.0f);
    when(paymentModeMapper.toEntity(paymentModeDto)).thenReturn(paymentModeEntity);
    when(transactionRepo.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(expectedResponse);

    TransactionDetailsResponse actual = transferStrategy.execute(context);

    assertNotNull(actual);
    assertEquals(expectedResponse, actual);

    verify(accountService, times(1)).debitAccount(userId, sourceAccountId, 500.0f);
    verify(accountService, times(1)).creditAccount(userId, destAccountId, 500.0f);

    ArgumentCaptor<Transaction> txnCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionRepo, times(2)).save(txnCaptor.capture());

    List<Transaction> savedTxns = txnCaptor.getAllValues();
    Transaction debitSaved = savedTxns.get(0);
    Transaction creditSaved = savedTxns.get(1);

    assertEquals(-500.0f, debitSaved.getAmount());
    assertEquals(sourceAccountUuid, debitSaved.getAccount().getId());
    assertEquals(Transaction.TransactionType.TRANSFER, debitSaved.getType());
    assertEquals("Account transfer", debitSaved.getDescription());
    assertNotNull(debitSaved.getTransferId());

    assertEquals(500.0f, creditSaved.getAmount());
    assertEquals(destAccountUuid, creditSaved.getAccount().getId());
    assertEquals(Transaction.TransactionType.TRANSFER, creditSaved.getType());
    assertEquals("Account transfer", creditSaved.getDescription());
    assertNotNull(creditSaved.getTransferId());

    assertEquals(debitSaved.getTransferId(), creditSaved.getTransferId());
    verify(transactionMapper, times(1)).toResponseDto(creditSaved);
  }

  @Test
  void delete_ShouldRevertBalancesUnlinkTasksAndBatchDelete() {
    UUID transferId = UUID.randomUUID();
    UUID debitTxnId = UUID.randomUUID();
    UUID creditTxnId = UUID.randomUUID();

    Account sourceAccount = Account.builder().id(sourceAccountUuid).balance(1000.0f).build();
    Account destAccount = Account.builder().id(destAccountUuid).balance(2000.0f).build();

    Transaction debitTxn = Transaction.builder()
        .id(debitTxnId)
        .account(sourceAccount)
        .amount(-500.0f)
        .transferId(transferId)
        .build();

    Transaction creditTxn = Transaction.builder()
        .id(creditTxnId)
        .account(destAccount)
        .amount(500.0f)
        .transferId(transferId)
        .build();

    List<Transaction> transferTransactions = List.of(debitTxn, creditTxn);

    when(transactionRepo.findAllByTransferIdAndAppUserId(transferId.toString(), userId))
        .thenReturn(transferTransactions);
    doNothing().when(aiParseTaskService).unlinkTransaction(debitTxnId);
    doNothing().when(aiParseTaskService).unlinkTransaction(creditTxnId);
    doNothing().when(transactionRepo).deleteAll(transferTransactions);

    Transaction inputTxn = Transaction.builder().transferId(transferId).build();
    transferStrategy.delete(userId, inputTxn);

    // Negative amount transaction reverted -> credited back
    assertEquals(1500.0f, sourceAccount.getBalance());
    verify(accountService, times(1)).saveAccount(sourceAccount);

    // Positive amount transaction reverted -> debited back
    assertEquals(1500.0f, destAccount.getBalance());
    verify(accountService, times(1)).saveAccount(destAccount);

    verify(aiParseTaskService, times(1)).unlinkTransaction(debitTxnId);
    verify(aiParseTaskService, times(1)).unlinkTransaction(creditTxnId);
    verify(transactionRepo, times(1)).deleteAll(transferTransactions);
  }

  @Test
  void delete_ShouldHandleNullAccountInTransactions() {
    UUID transferId = UUID.randomUUID();
    UUID txnId = UUID.randomUUID();

    Transaction txn = Transaction.builder()
        .id(txnId)
        .account(null)
        .amount(500.0f)
        .transferId(transferId)
        .build();

    List<Transaction> transferTransactions = List.of(txn);

    when(transactionRepo.findAllByTransferIdAndAppUserId(transferId.toString(), userId))
        .thenReturn(transferTransactions);
    doNothing().when(aiParseTaskService).unlinkTransaction(txnId);
    doNothing().when(transactionRepo).deleteAll(transferTransactions);

    Transaction inputTxn = Transaction.builder().transferId(transferId).build();
    transferStrategy.delete(userId, inputTxn);

    verify(accountService, never()).saveAccount(any());
    verify(aiParseTaskService, times(1)).unlinkTransaction(txnId);
    verify(transactionRepo, times(1)).deleteAll(transferTransactions);
  }

  @Test
  void delete_ShouldHandleEmptyTransferTransactions() {
    UUID transferId = UUID.randomUUID();

    when(transactionRepo.findAllByTransferIdAndAppUserId(transferId.toString(), userId))
        .thenReturn(Collections.emptyList());

    Transaction inputTxn = Transaction.builder().transferId(transferId).build();
    transferStrategy.delete(userId, inputTxn);

    verify(accountService, never()).saveAccount(any());
    verify(aiParseTaskService, never()).unlinkTransaction(any());
    verify(transactionRepo, times(1)).deleteAll(Collections.emptyList());
  }

  @Test
  void getType_ShouldReturnTransfer() {
    assertEquals(Transaction.TransactionType.TRANSFER, transferStrategy.getType());
  }
}
