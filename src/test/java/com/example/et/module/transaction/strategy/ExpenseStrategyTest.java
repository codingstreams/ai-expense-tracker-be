package com.example.et.module.transaction.strategy;

import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.module.account.Account;
import com.example.et.module.account.AccountService;
import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.ai.parser.AiParseTaskService;
import com.example.et.module.card.Card;
import com.example.et.module.card.CardService;
import com.example.et.module.reference.paymentmode.PaymentModeMapper;
import com.example.et.module.reference.paymentmode.dto.PaymentModeDetailsResponse;
import com.example.et.module.transaction.Transaction;
import com.example.et.module.transaction.TransactionContext;
import com.example.et.module.transaction.TransactionMapper;
import com.example.et.module.transaction.dto.CreateTransactionRequest;
import com.example.et.module.transaction.dto.TransactionDetailsResponse;
import com.example.et.module.transaction.internal.TransactionRepo;
import com.example.et.module.transaction.internal.strategy.ExpenseStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseStrategyTest {

  @Mock
  private CardService cardService;

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
  private ExpenseStrategy expenseStrategy;

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
  void execute_ShouldDebitAccountAndSaveTransaction_WhenAccountIdProvided() {
    CreateTransactionRequest request = new CreateTransactionRequest(
        null,
        Transaction.TransactionType.EXPENSE,
        150.0f,
        LocalDate.now(),
        "Groceries",
        accountId,
        null,
        null,
        null,
        null,
        null
    );

    PaymentModeDetailsResponse paymentMode = new PaymentModeDetailsResponse(UUID.randomUUID(), "UPI");
    TransactionContext context = new TransactionContext(userId, request, paymentMode, null);
    AccountDetailsResponse accountDetailsResponse = new AccountDetailsResponse(accountUuid, "1234", 1000.0f, Account.AccountType.SAVINGS, true, true, null, true);
    Transaction savedTransaction = Transaction.builder().id(UUID.randomUUID()).amount(150.0f).build();
    TransactionDetailsResponse expectedResponse = new TransactionDetailsResponse(
        savedTransaction.getId(), Transaction.TransactionType.EXPENSE, 150.0f, LocalDate.now(), "Groceries", "Acc", "UPI", null
    );

    doNothing().when(accountService).debitAccount(userId, accountId, 150.0f);
    when(accountService.getAccount(userId, accountId)).thenReturn(accountDetailsResponse);
    when(transactionRepo.save(any(Transaction.class))).thenReturn(savedTransaction);
    when(transactionMapper.toResponseDto(savedTransaction)).thenReturn(expectedResponse);

    TransactionDetailsResponse actual = expenseStrategy.execute(context);

    assertNotNull(actual);
    assertEquals(expectedResponse, actual);
    verify(accountService, times(1)).debitAccount(userId, accountId, 150.0f);
    verify(transactionRepo, times(1)).save(any(Transaction.class));
  }

  @Test
  void execute_ShouldResolveAccountFromCard_WhenCardIdProvided() {
    UUID cardUuid = UUID.randomUUID();
    CreateTransactionRequest request = new CreateTransactionRequest(
        null,
        Transaction.TransactionType.EXPENSE,
        200.0f,
        LocalDate.now(),
        "Fuel",
        null,
        cardUuid.toString(),
        null,
        null,
        null,
        null
    );

    Account linkedAccount = Account.builder().id(accountUuid).build();
    Card card = Card.builder().id(cardUuid).account(linkedAccount).build();

    TransactionContext context = new TransactionContext(userId, request, null, null);
    Transaction savedTxn = Transaction.builder().id(UUID.randomUUID()).amount(200.0f).build();
    TransactionDetailsResponse expectedResponse = new TransactionDetailsResponse(
        savedTxn.getId(), Transaction.TransactionType.EXPENSE, 200.0f, LocalDate.now(), "Fuel", "Acc", "CARD", null
    );

    doNothing().when(accountService).debitAccount(userId, null, 200.0f);
    when(cardService.getUserCard(userId, cardUuid)).thenReturn(card);
    when(transactionRepo.save(any(Transaction.class))).thenReturn(savedTxn);
    when(transactionMapper.toResponseDto(savedTxn)).thenReturn(expectedResponse);

    TransactionDetailsResponse actual = expenseStrategy.execute(context);

    assertNotNull(actual);
    assertEquals(expectedResponse, actual);
    verify(cardService, times(1)).getUserCard(userId, cardUuid);
  }

  @Test
  void execute_ShouldThrowApiException_WhenCardNotLinked() {
    UUID cardUuid = UUID.randomUUID();
    CreateTransactionRequest request = new CreateTransactionRequest(
        null,
        Transaction.TransactionType.EXPENSE,
        100.0f,
        LocalDate.now(),
        "Coffee",
        null,
        cardUuid.toString(),
        null,
        null,
        null,
        null
    );

    Card cardWithoutAccount = Card.builder().id(cardUuid).account(null).build();
    TransactionContext context = new TransactionContext(userId, request, null, null);

    doNothing().when(accountService).debitAccount(userId, null, 100.0f);
    when(cardService.getUserCard(userId, cardUuid)).thenReturn(cardWithoutAccount);

    ApiException ex = assertThrows(ApiException.class, () -> expenseStrategy.execute(context));

    assertEquals(ErrorCode.CARD_NOT_LINKED, ex.getErrorCode());
    verify(transactionRepo, never()).save(any());
  }

  @Test
  void execute_ShouldThrowApiException_WhenNeitherAccountNorCardProvided() {
    CreateTransactionRequest request = new CreateTransactionRequest(
        null,
        Transaction.TransactionType.EXPENSE,
        100.0f,
        LocalDate.now(),
        "Lunch",
        null,
        null,
        null,
        null,
        null,
        null
    );

    TransactionContext context = new TransactionContext(userId, request, null, null);
    doNothing().when(accountService).debitAccount(userId, null, 100.0f);

    ApiException ex = assertThrows(ApiException.class, () -> expenseStrategy.execute(context));

    assertEquals(ErrorCode.INVALID_TRANSACTION_PAYLOAD, ex.getErrorCode());
    verify(transactionRepo, never()).save(any());
  }

  @Test
  void delete_ShouldCreditAccountAndUnlinkAiTask() {
    UUID txId = UUID.randomUUID();
    Account account = Account.builder().id(accountUuid).build();
    Transaction transaction = Transaction.builder()
        .id(txId)
        .account(account)
        .amount(250.0f)
        .build();

    doNothing().when(accountService).creditAccount(userId, accountUuid.toString(), 250.0f);
    doNothing().when(aiParseTaskService).unlinkTransaction(txId);
    doNothing().when(transactionRepo).delete(transaction);

    expenseStrategy.delete(userId, transaction);

    verify(accountService, times(1)).creditAccount(userId, accountUuid.toString(), 250.0f);
    verify(aiParseTaskService, times(1)).unlinkTransaction(txId);
    verify(transactionRepo, times(1)).delete(transaction);
  }

  @Test
  void getType_ShouldReturnExpense() {
    assertEquals(Transaction.TransactionType.EXPENSE, expenseStrategy.getType());
  }
}
