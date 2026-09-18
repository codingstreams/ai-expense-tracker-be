package com.example.et.module.card;

import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.module.account.Account;
import com.example.et.module.account.AccountMapper;
import com.example.et.module.account.AccountService;
import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.card.dto.AddCardsRequest;
import com.example.et.module.card.dto.CardResponse;
import com.example.et.module.card.dto.CardsResponse;
import com.example.et.module.card.internal.CardRepo;
import com.example.et.module.card.internal.CardServiceImpl;
import com.example.et.module.reference.bank.Bank;
import com.example.et.module.user.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

  @Mock
  private CardRepo cardRepo;

  @Mock
  private AccountService accountService;

  @Mock
  private CardMapper cardMapper;

  @Mock
  private AccountMapper accountMapper;

  @InjectMocks
  private CardServiceImpl cardService;

  private UUID userUuid;
  private String userId;
  private AppUser appUser;
  private Bank sampleBank;

  @BeforeEach
  void setUp() {
    userUuid = UUID.randomUUID();
    userId = userUuid.toString();
    appUser = AppUser.ofId(userId);
    sampleBank = Bank.builder().id(UUID.randomUUID()).name("HDFC").build();
  }

  // ----------------------------------------------------------------------
  // getUserCards
  // ----------------------------------------------------------------------
  @Test
  void getUserCards_ShouldReturnAllCards_WhenCardTypeIsNull() {
    Card creditCard = Card.builder()
        .id(UUID.randomUUID())
        .cardType(Card.CardType.CREDIT_CARD)
        .lastFourDigits("1111")
        .appUser(appUser)
        .build();

    Card debitCard = Card.builder()
        .id(UUID.randomUUID())
        .cardType(Card.CardType.DEBIT_CARD)
        .lastFourDigits("2222")
        .appUser(appUser)
        .build();

    CardResponse creditDto = new CardResponse(creditCard.getId(), Card.CardType.CREDIT_CARD, "1111", null, 50000.0f, sampleBank);
    CardResponse debitDto = new CardResponse(debitCard.getId(), Card.CardType.DEBIT_CARD, "2222", "acc-1", null, sampleBank);

    when(cardRepo.findByAppUserId(userUuid)).thenReturn(List.of(creditCard, debitCard));
    when(cardMapper.toDto(creditCard)).thenReturn(creditDto);
    when(cardMapper.toDto(debitCard)).thenReturn(debitDto);

    CardsResponse response = cardService.getUserCards(userId, null);

    assertNotNull(response);
    assertEquals(2, response.cards().size());
    verify(cardRepo, times(1)).findByAppUserId(userUuid);
  }

  @Test
  void getUserCards_ShouldFilterByCardType_WhenCardTypeIsCreditCard() {
    Card creditCard = Card.builder()
        .id(UUID.randomUUID())
        .cardType(Card.CardType.CREDIT_CARD)
        .lastFourDigits("1111")
        .appUser(appUser)
        .build();

    Card debitCard = Card.builder()
        .id(UUID.randomUUID())
        .cardType(Card.CardType.DEBIT_CARD)
        .lastFourDigits("2222")
        .appUser(appUser)
        .build();

    CardResponse creditDto = new CardResponse(creditCard.getId(), Card.CardType.CREDIT_CARD, "1111", null, 50000.0f, sampleBank);

    when(cardRepo.findByAppUserId(userUuid)).thenReturn(List.of(creditCard, debitCard));
    when(cardMapper.toDto(creditCard)).thenReturn(creditDto);

    CardsResponse response = cardService.getUserCards(userId, Card.CardType.CREDIT_CARD);

    assertNotNull(response);
    assertEquals(1, response.cards().size());
    assertEquals(Card.CardType.CREDIT_CARD, response.cards().get(0).cardType());
    verify(cardMapper, never()).toDto(debitCard);
  }

  @Test
  void getUserCards_ShouldReturnEmptyCardsResponse_WhenNoCardsExist() {
    when(cardRepo.findByAppUserId(userUuid)).thenReturn(Collections.emptyList());

    CardsResponse response = cardService.getUserCards(userId, Card.CardType.DEBIT_CARD);

    assertNotNull(response);
    assertTrue(response.cards().isEmpty());
    verify(cardRepo, times(1)).findByAppUserId(userUuid);
  }

  // ----------------------------------------------------------------------
  // addCards
  // ----------------------------------------------------------------------
  @Test
  void addCards_ShouldCreateCreditAccountAndSaveCard_WhenCardIsCreditCard() {
    CardResponse requestCard = new CardResponse(null, Card.CardType.CREDIT_CARD, "4321", null, 75000.0f, sampleBank);
    AddCardsRequest request = new AddCardsRequest(List.of(requestCard));

    Account savedCreditAccount = Account.builder()
        .id(UUID.randomUUID())
        .accountType(Account.AccountType.CREDIT)
        .balance(75000.0f)
        .lastFourDigits("4321")
        .bank(sampleBank)
        .isActive(true)
        .build();

    Card savedCard = Card.builder()
        .id(UUID.randomUUID())
        .cardType(Card.CardType.CREDIT_CARD)
        .lastFourDigits("4321")
        .account(savedCreditAccount)
        .appUser(appUser)
        .build();

    CardResponse responseCard = new CardResponse(savedCard.getId(), Card.CardType.CREDIT_CARD, "4321", savedCreditAccount.getId().toString(), 75000.0f, sampleBank);

    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    when(accountService.saveAccount(accountCaptor.capture())).thenReturn(savedCreditAccount);
    when(cardRepo.saveAll(any())).thenReturn(List.of(savedCard));
    when(cardMapper.toDto(savedCard)).thenReturn(responseCard);

    CardsResponse result = cardService.addCards(userId, request);

    assertNotNull(result);
    assertEquals(1, result.cards().size());
    assertEquals(responseCard, result.cards().get(0));

    Account createdAccount = accountCaptor.getValue();
    assertEquals(Account.AccountType.CREDIT, createdAccount.getAccountType());
    assertEquals(75000.0f, createdAccount.getBalance());
    assertEquals("4321", createdAccount.getLastFourDigits());
    assertEquals(sampleBank, createdAccount.getBank());
    assertTrue(createdAccount.isActive());
  }

  @Test
  void addCards_ShouldUseZeroLimit_WhenCreditCardLimitIsNull() {
    CardResponse requestCard = new CardResponse(null, Card.CardType.CREDIT_CARD, "4321", null, null, sampleBank);
    AddCardsRequest request = new AddCardsRequest(List.of(requestCard));

    Account savedCreditAccount = Account.builder()
        .id(UUID.randomUUID())
        .accountType(Account.AccountType.CREDIT)
        .balance(0.0f)
        .lastFourDigits("4321")
        .isActive(true)
        .build();

    Card savedCard = Card.builder()
        .id(UUID.randomUUID())
        .cardType(Card.CardType.CREDIT_CARD)
        .account(savedCreditAccount)
        .build();

    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    when(accountService.saveAccount(accountCaptor.capture())).thenReturn(savedCreditAccount);
    when(cardRepo.saveAll(any())).thenReturn(List.of(savedCard));
    when(cardMapper.toDto(savedCard)).thenReturn(requestCard);

    cardService.addCards(userId, request);

    Account captured = accountCaptor.getValue();
    assertEquals(0.0f, captured.getBalance());
  }

  @Test
  void addCards_ShouldLinkExistingAccount_WhenCardIsDebitCard() {
    String existingAccountId = UUID.randomUUID().toString();
    CardResponse requestCard = new CardResponse(null, Card.CardType.DEBIT_CARD, "8888", existingAccountId, null, null);
    AddCardsRequest request = new AddCardsRequest(List.of(requestCard));

    AccountDetailsResponse existingAccountDetailsResponse = new AccountDetailsResponse(UUID.fromString(existingAccountId), "8888", 1000.0f, Account.AccountType.SAVINGS, true, true, null, true);
    Account existingAccount = Account.builder().id(UUID.fromString(existingAccountId)).balance(1000.0f).build();

    Card savedCard = Card.builder()
        .id(UUID.randomUUID())
        .cardType(Card.CardType.DEBIT_CARD)
        .lastFourDigits("8888")
        .account(existingAccount)
        .appUser(appUser)
        .build();

    CardResponse responseCard = new CardResponse(savedCard.getId(), Card.CardType.DEBIT_CARD, "8888", existingAccountId, null, null);

    when(accountService.getAccount(userId, existingAccountId)).thenReturn(existingAccountDetailsResponse);
    when(accountMapper.toEntity(existingAccountDetailsResponse, userId)).thenReturn(existingAccount);
    when(cardRepo.saveAll(any())).thenReturn(List.of(savedCard));
    when(cardMapper.toDto(savedCard)).thenReturn(responseCard);

    CardsResponse result = cardService.addCards(userId, request);

    assertNotNull(result);
    assertEquals(1, result.cards().size());
    verify(accountService, never()).saveAccount(any());
    verify(accountService, times(1)).getAccount(userId, existingAccountId);
  }

  // ----------------------------------------------------------------------
  // getUserCard
  // ----------------------------------------------------------------------
  @Test
  void getUserCard_ShouldReturnCard_WhenCardExists() {
    UUID cardId = UUID.randomUUID();
    Card card = Card.builder().id(cardId).appUser(appUser).cardType(Card.CardType.CREDIT_CARD).build();

    when(cardRepo.findByIdAndAppUserId(cardId, userUuid)).thenReturn(Optional.of(card));

    Card result = cardService.getUserCard(userId, cardId);

    assertNotNull(result);
    assertEquals(cardId, result.getId());
    verify(cardRepo, times(1)).findByIdAndAppUserId(cardId, userUuid);
  }

  @Test
  void getUserCard_ShouldThrowApiException_WhenCardNotFound() {
    UUID cardId = UUID.randomUUID();

    when(cardRepo.findByIdAndAppUserId(cardId, userUuid)).thenReturn(Optional.empty());

    ApiException ex = assertThrows(ApiException.class, () -> cardService.getUserCard(userId, cardId));

    assertEquals(ErrorCode.CARD_NOT_FOUND, ex.getErrorCode());
    verify(cardRepo, times(1)).findByIdAndAppUserId(cardId, userUuid);
  }
}
