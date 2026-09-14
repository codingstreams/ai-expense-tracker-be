package com.example.et.module.account;

import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.core.exception.InsufficientAccountBalanceException;
import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.account.dto.AccountDtoOld;
import com.example.et.module.account.dto.CreateAccountsRequest;
import com.example.et.module.account.dto.UpdateCashBalanceRequest;
import com.example.et.module.account.internal.AccountRepository;
import com.example.et.module.account.internal.AccountServiceImpl;
import com.example.et.module.reference.bank.Bank;
import com.example.et.module.reference.bank.BankService;
import com.example.et.module.reference.bank.dto.BankDetailsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private AccountMapper accountMapper;

  @Mock
  private BankService bankService;

  @InjectMocks
  private AccountServiceImpl accountService;

  private UUID userUuid;
  private String userId;
  private UUID accountUuid;
  private String accountId;
  private Account sampleAccount;
  private AccountDetailsResponse sampleAccountDetailsResponse;
  private BankDetailsResponse sampleBankDetailsResponse;

  @BeforeEach
  void setUp() {
    userUuid = UUID.randomUUID();
    userId = userUuid.toString();
    accountUuid = UUID.randomUUID();
    accountId = accountUuid.toString();
    sampleBankDetailsResponse = new BankDetailsResponse(UUID.randomUUID(), "Test Bank");

    sampleAccount = Account.builder()
        .id(accountUuid)
        .accountType(Account.AccountType.SAVINGS)
        .balance(500.0f)
        .lastFourDigits("1234")
        .upiEnabled(true)
        .netBankingEnabled(true)
        .isActive(true)
        .build();

    sampleAccountDetailsResponse = new AccountDetailsResponse(
        accountUuid,
        "1234",
        500.0f,
        Account.AccountType.SAVINGS,
        true,
        true,
        sampleBankDetailsResponse,
        true
    );
  }

  // ----------------------------------------------------------------------
  // getUserAccountList
  // ----------------------------------------------------------------------
  @Test
  void getUserAccountList_ShouldReturnListOfAccounts() {
    when(accountRepository.findByAppUserId(userUuid)).thenReturn(List.of(sampleAccount));

    List<Account> result = accountService.getUserAccountList(userId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(accountUuid, result.get(0).getId());
    verify(accountRepository, times(1)).findByAppUserId(userUuid);
  }

  // ----------------------------------------------------------------------
  // addAccounts
  // ----------------------------------------------------------------------
  @Test
  void addAccounts_ShouldValidateBankIdsAndSaveAccounts_WhenValid() {
    CreateAccountsRequest request = new CreateAccountsRequest(List.of(sampleAccountDetailsResponse));

    doNothing().when(bankService).validateBankIds(any());
    when(accountMapper.toEntity(sampleAccountDetailsResponse)).thenReturn(sampleAccount);
    when(accountRepository.saveAll(List.of(sampleAccount))).thenReturn(List.of(sampleAccount));
    when(accountMapper.toDto(sampleAccount)).thenReturn(sampleAccountDetailsResponse);

    List<AccountDetailsResponse> result = accountService.addAccounts(userId, request);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(sampleAccountDetailsResponse, result.get(0));

    verify(bankService, times(1)).validateBankIds(any());
    verify(accountRepository, times(1)).saveAll(List.of(sampleAccount));
  }

  @Test
  void addAccounts_ShouldThrowApiException_WhenBankValidationFails() {
    CreateAccountsRequest request = new CreateAccountsRequest(List.of(sampleAccountDetailsResponse));

    doThrow(new ApiException(ErrorCode.INVALID_BANK_IDS)).when(bankService).validateBankIds(any());

    ApiException ex = assertThrows(ApiException.class, () -> accountService.addAccounts(userId, request));

    assertEquals(ErrorCode.INVALID_BANK_IDS, ex.getErrorCode());
    verify(accountRepository, never()).saveAll(any());
  }

  // ----------------------------------------------------------------------
  // saveAccount
  // ----------------------------------------------------------------------
  @Test
  void saveAccount_ShouldSaveAndReturnAccount() {
    when(accountRepository.save(sampleAccount)).thenReturn(sampleAccount);

    Account result = accountService.saveAccount(sampleAccount);

    assertNotNull(result);
    assertEquals(sampleAccount, result);
    verify(accountRepository, times(1)).save(sampleAccount);
  }

  // ----------------------------------------------------------------------
  // getUserAccountDetails
  // ----------------------------------------------------------------------
  @Test
  void getUserAccountDetails_ShouldReturnAccountDto_WhenFound() {
    when(accountRepository.findByUserIdAndAccountId(userUuid, accountUuid)).thenReturn(Optional.of(sampleAccount));
    when(accountMapper.toDto(sampleAccount)).thenReturn(sampleAccountDetailsResponse);

    AccountDetailsResponse result = accountService.getUserAccountDetails(userId, accountId);

    assertNotNull(result);
    assertEquals(sampleAccountDetailsResponse, result);
    verify(accountRepository, times(1)).findByUserIdAndAccountId(userUuid, accountUuid);
  }

  @Test
  void getUserAccountDetails_ShouldThrowApiException_WhenNotFound() {
    when(accountRepository.findByUserIdAndAccountId(userUuid, accountUuid)).thenReturn(Optional.empty());

    ApiException ex = assertThrows(ApiException.class, () -> accountService.getUserAccountDetails(userId, accountId));

    assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
    verify(accountRepository, times(1)).findByUserIdAndAccountId(userUuid, accountUuid);
  }

  // ----------------------------------------------------------------------
  // getAccount & getAccountEntity
  // ----------------------------------------------------------------------
  @Test
  void getAccount_ShouldReturnAccountDto_WhenFound() {
    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));
    when(accountMapper.toDto(sampleAccount)).thenReturn(sampleAccountDetailsResponse);

    AccountDetailsResponse result = accountService.getAccount(userId, accountId);

    assertNotNull(result);
    assertEquals(sampleAccountDetailsResponse, result);
    verify(accountRepository, times(1)).findByIdAndAppUserId(accountUuid, userUuid);
  }

  @Test
  void getAccount_ShouldThrowApiException_WhenNotFound() {
    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.empty());

    ApiException ex = assertThrows(ApiException.class, () -> accountService.getAccount(userId, accountId));

    assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
    verify(accountRepository, times(1)).findByIdAndAppUserId(accountUuid, userUuid);
  }

  @Test
  void getAccountEntity_ShouldReturnEntity_WhenFound() {
    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));

    Account result = accountService.getAccountEntity(userId, accountId);

    assertNotNull(result);
    assertEquals(sampleAccount, result);
  }

  // ----------------------------------------------------------------------
  // updateAccount
  // ----------------------------------------------------------------------
  @Test
  void updateAccount_ShouldUpdateFieldsAndSave_WhenChangesPresent() {
    // Existing: balance 500, lastFourDigits 1234
    AccountDetailsResponse updateDto = new AccountDetailsResponse(
        accountUuid,
        "5678",
        800.0f,
        Account.AccountType.SAVINGS,
        false,
        false,
        sampleBankDetailsResponse,
        true
    );

    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));
    when(accountRepository.save(sampleAccount)).thenReturn(sampleAccount);
    when(accountMapper.toDto(sampleAccount)).thenReturn(updateDto);

    AccountDetailsResponse result = accountService.updateAccount(userId, accountId, updateDto);

    assertNotNull(result);
    assertEquals("5678", sampleAccount.getLastFourDigits());
    assertEquals(800.0f, sampleAccount.getBalance());
    assertFalse(sampleAccount.isUpiEnabled());
    assertFalse(sampleAccount.isNetBankingEnabled());
    verify(accountRepository, times(1)).save(sampleAccount);
  }

  @Test
  void updateAccount_ShouldNotSave_WhenNoChangesRequired() {
    AccountDetailsResponse sameDto = new AccountDetailsResponse(
        accountUuid,
        "1234",
        400.0f, // lower balance won't trigger update
        Account.AccountType.SAVINGS,
        true,
        true,
        sampleBankDetailsResponse,
        true
    );

    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));
    when(accountMapper.toDto(sampleAccount)).thenReturn(sameDto);

    AccountDetailsResponse result = accountService.updateAccount(userId, accountId, sameDto);

    assertNotNull(result);
    verify(accountRepository, never()).save(any());
  }

  // ----------------------------------------------------------------------
  // deleteAccount
  // ----------------------------------------------------------------------
  @Test
  void deleteAccount_ShouldDeactivateAccount_WhenNotCashAccount() {
    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));

    accountService.deleteAccount(userId, accountId);

    assertFalse(sampleAccount.getIsActive());
    verify(accountRepository, times(1)).save(sampleAccount);
  }

  @Test
  void deleteAccount_ShouldThrowApiException_WhenAccountIsCash() {
    Account cashAccount = Account.builder()
        .id(accountUuid)
        .accountType(Account.AccountType.CASH)
        .balance(100.0f)
        .lastFourDigits("CASH")
        .isActive(true)
        .build();

    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(cashAccount));

    ApiException ex = assertThrows(ApiException.class, () -> accountService.deleteAccount(userId, accountId));

    assertEquals(ErrorCode.CASH_ACCOUNT_IMMUTABLE, ex.getErrorCode());
    verify(accountRepository, never()).save(any());
  }

  // ----------------------------------------------------------------------
  // updateCashBalance
  // ----------------------------------------------------------------------
  @Test
  void updateCashBalance_Float_ShouldUpdateBalance_WhenCashAccountFound() {
    Account cashAccount = Account.builder()
        .id(accountUuid)
        .accountType(Account.AccountType.CASH)
        .balance(100.0f)
        .lastFourDigits("CASH")
        .isActive(true)
        .build();

    when(accountRepository.findCashAccountByUserId(userUuid)).thenReturn(Optional.of(cashAccount));

    Float result = accountService.updateCashBalance(userId, 350.0f);

    assertEquals(350.0f, result);
    assertEquals(350.0f, cashAccount.getBalance());
    verify(accountRepository, times(1)).save(cashAccount);
  }

  @Test
  void updateCashBalance_Float_ShouldThrowApiException_WhenCashAccountNotFound() {
    when(accountRepository.findCashAccountByUserId(userUuid)).thenReturn(Optional.empty());

    ApiException ex = assertThrows(ApiException.class, () -> accountService.updateCashBalance(userId, 350.0f));

    assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, ex.getErrorCode());
    verify(accountRepository, never()).save(any());
  }

  @Test
  void updateCashBalance_Dto_ShouldUpdateBalance_WhenCashAccountFound() {
    Account cashAccount = Account.builder()
        .id(accountUuid)
        .accountType(Account.AccountType.CASH)
        .balance(100.0f)
        .lastFourDigits("CASH")
        .isActive(true)
        .build();

    UpdateCashBalanceRequest updateCashBalanceRequest = new UpdateCashBalanceRequest(450.0f);
    AccountDetailsResponse resultDto = new AccountDetailsResponse(accountUuid, "CASH", 450.0f, Account.AccountType.CASH, false, false, null, true);

    when(accountRepository.findCashAccountByUserId(userUuid)).thenReturn(Optional.of(cashAccount));
    when(accountRepository.save(cashAccount)).thenReturn(cashAccount);
    when(accountMapper.toDto(cashAccount)).thenReturn(resultDto);

    AccountDetailsResponse result = accountService.updateCashBalance(userId, updateCashBalanceRequest);

    assertNotNull(result);
    assertEquals(450.0f, result.balance());
    verify(accountRepository, times(1)).save(cashAccount);
  }

  // ----------------------------------------------------------------------
  // getUserCashAccountDetails & getUserAccounts
  // ----------------------------------------------------------------------
  @Test
  void getUserCashAccountDetails_ShouldReturnOldDto() {
    AccountDtoOld oldDto = new AccountDtoOld(
        accountUuid,
        "CASH",
        200.0f,
        Account.AccountType.CASH,
        new Bank(UUID.randomUUID(), "Cash"),
        false,
        false
    );

    when(accountRepository.findByUserIdAndAccountType(userUuid, Account.AccountType.CASH)).thenReturn(oldDto);

    AccountDtoOld result = accountService.getUserCashAccountDetails(userId);

    assertNotNull(result);
    assertEquals(oldDto, result);
    verify(accountRepository, times(1)).findByUserIdAndAccountType(userUuid, Account.AccountType.CASH);
  }

  @Test
  void getUserAccounts_ShouldReturnListOfDtos() {
    when(accountRepository.findByAppUserId(userUuid)).thenReturn(List.of(sampleAccount));
    when(accountMapper.toDto(sampleAccount)).thenReturn(sampleAccountDetailsResponse);

    List<AccountDetailsResponse> result = accountService.getUserAccounts(userId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(sampleAccountDetailsResponse, result.get(0));
    verify(accountRepository, times(1)).findByAppUserId(userUuid);
  }

  // ----------------------------------------------------------------------
  // debitAccount
  // ----------------------------------------------------------------------
  @Test
  void debitAccount_ShouldDeductBalanceAndSave_WhenSufficientBalance() {
    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));

    accountService.debitAccount(userId, accountId, 200.0f);

    assertEquals(300.0f, sampleAccount.getBalance());
    verify(accountRepository, times(1)).save(sampleAccount);
  }

  @Test
  void debitAccount_ShouldThrowInsufficientAccountBalanceException_WhenBalanceInsufficient() {
    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));

    assertThrows(InsufficientAccountBalanceException.class, () -> accountService.debitAccount(userId, accountId, 600.0f));

    verify(accountRepository, never()).save(any());
  }

  @Test
  void debitAccount_ShouldThrowApiException_WhenAmountIsZeroOrNegative() {
    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));

    ApiException ex = assertThrows(ApiException.class, () -> accountService.debitAccount(userId, accountId, -10.0f));

    assertEquals(ErrorCode.INVALID_AMOUNT, ex.getErrorCode());
    verify(accountRepository, never()).save(any());
  }

  // ----------------------------------------------------------------------
  // creditAccount
  // ----------------------------------------------------------------------
  @Test
  void creditAccount_ShouldIncreaseBalanceAndSave_WhenValidAmount() {
    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));

    accountService.creditAccount(userId, accountId, 250.0f);

    assertEquals(750.0f, sampleAccount.getBalance());
    verify(accountRepository, times(1)).save(sampleAccount);
  }

  @Test
  void creditAccount_ShouldThrowApiException_WhenAmountIsZeroOrNegative() {
    when(accountRepository.findByIdAndAppUserId(accountUuid, userUuid)).thenReturn(Optional.of(sampleAccount));

    ApiException ex = assertThrows(ApiException.class, () -> accountService.creditAccount(userId, accountId, 0.0f));

    assertEquals(ErrorCode.INVALID_AMOUNT, ex.getErrorCode());
    verify(accountRepository, never()).save(any());
  }
}
