package com.example.et.module.account.internal;

import com.example.et.core.config.CacheNames;
import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.module.account.Account;
import com.example.et.module.account.AccountMapper;
import com.example.et.module.account.AccountService;
import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.account.dto.AccountDtoOld;
import com.example.et.module.account.dto.CreateAccountsRequest;
import com.example.et.module.account.dto.UpdateCashBalanceRequest;
import com.example.et.module.reference.bank.BankService;
import com.example.et.module.reference.bank.dto.BankDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final AccountRepository accountRepository;
  private final AccountMapper accountMapper;
  private final BankService bankService;

  @Override
  public List<Account> getUserAccountList(String userId) {
    return accountRepository.findByAppUserId(UUID.fromString(userId));
  }

  @Override
  @Caching(
      evict = {
          @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
          @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
      }
  )
  public List<AccountDetailsResponse> addAccounts(String userId, CreateAccountsRequest requestBody) {
    final var bankIds = requestBody.accounts()
        .stream()
        .map(AccountDetailsResponse::bank)
        .map(BankDetailsResponse::id)
        .collect(Collectors.toSet());

    bankService.validateBankIds(bankIds);

    final var accountToBeCreated = requestBody.accounts()
        .stream()
        .map(dto -> accountMapper.toEntity(dto, userId))
        .toList();

    return accountRepository.saveAll(accountToBeCreated)
        .stream()
        .map(accountMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#account.appUser.id", condition = "#account.appUser != null && #account.appUser.id != null"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#account.appUser.id, #account.id}", condition = "#account.appUser != null && #account.appUser.id != null && #account.id != null"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#account.appUser.id, 'cash'}", condition = "#account.appUser != null && #account.appUser.id != null && #account.accountType != null && #account.accountType.name() == 'CASH'"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
  })
  public Account saveAccount(Account account) {
    return accountRepository.save(account);
  }

  @Override
  @Cacheable(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, #accountId}")
  public AccountDetailsResponse getUserAccountDetails(String userId, String accountId) {
    return accountRepository.findByUserIdAndAccountId(UUID.fromString(userId), UUID.fromString(accountId))
        .map(accountMapper::toDto)
        .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, #accountId}"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
  })
  public AccountDetailsResponse updateAccount(String userId, String accountId, AccountDetailsResponse account) {
    final var existingAccount = getAccountEntity(userId, accountId);

    boolean isUpdateRequired = false;

    if (account != null) {
      if (account.balance() != null && (existingAccount.getBalance() == null || account.balance().compareTo(existingAccount.getBalance()) > 0)) {
        existingAccount.setBalance(account.balance());
        isUpdateRequired = true;
      }
      if (account.lastFourDigits() != null && !Objects.equals(account.lastFourDigits(), existingAccount.getLastFourDigits())) {
        existingAccount.setLastFourDigits(account.lastFourDigits());
        isUpdateRequired = true;
      }
      if (account.accountType() != null && !Objects.equals(account.accountType(), existingAccount.getAccountType())) {
        existingAccount.setAccountType(account.accountType());
        isUpdateRequired = true;
      }
      // Disable Bank Update
/*      if (account.bank() != null) {
        existingAccount.setBank(account.bank());
      }*/

      if (account.upiEnabled() != null && !Objects.equals(account.upiEnabled(), existingAccount.isUpiEnabled())) {
        existingAccount.setUpiEnabled(account.upiEnabled());
        isUpdateRequired = true;
      }
      if (account.netBankingEnabled() != null && !Objects.equals(account.netBankingEnabled(), existingAccount.isNetBankingEnabled())) {
        existingAccount.setNetBankingEnabled(account.netBankingEnabled());
        isUpdateRequired = true;
      }
    }

    final var updatedAccount = isUpdateRequired ? accountRepository.save(existingAccount) : existingAccount;
    return accountMapper.toDto(updatedAccount);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, #accountId}"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
  })
  public void deleteAccount(String userId, String accountId) {
    final var existingAccount = getAccountEntity(userId, accountId);

    if (existingAccount.getAccountType().compareTo(Account.AccountType.CASH) == 0) {
      throw new ApiException(ErrorCode.CASH_ACCOUNT_IMMUTABLE, "Cannot delete cash account");
    }

    existingAccount.setIsActive(false);
    accountRepository.save(existingAccount);
  }

  @Override
  public Account getAccountEntity(String userId, String accountId) {
    return accountRepository.findByIdAndAppUserId(UUID.fromString(accountId), UUID.fromString(userId))
        .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
  }

  @Override
  public AccountDetailsResponse getAccount(String userId, String accountId) {
    return accountMapper.toDto(getAccountEntity(userId, accountId));
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
  })
  public Float updateCashBalance(String userId, Float cashBalance) {
    final var cashAccount = getCashAccount(userId);

    if (cashBalance > 0) {
      cashAccount.setBalance(cashBalance);
    }

    accountRepository.save(cashAccount);
    return cashBalance;
  }

  private @NonNull Account getCashAccount(String userId) {
    return accountRepository.findCashAccountByUserId(UUID.fromString(userId))
        .orElseThrow(() -> new ApiException(ErrorCode.ACCOUNT_NOT_FOUND));
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
  })
  public AccountDetailsResponse updateCashBalance(String userId, UpdateCashBalanceRequest updateCashBalanceRequest) {
    final var cashAccount = getCashAccount(userId);

    if (updateCashBalanceRequest.cashBalance() > 0) {
      cashAccount.setBalance(updateCashBalanceRequest.cashBalance());
    }

    return accountMapper.toDto(accountRepository.save(cashAccount));
  }

  @Override
  @Cacheable(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}")
  public AccountDtoOld getUserCashAccountDetails(String userId) {
    return accountRepository.findByUserIdAndAccountType(UUID.fromString(userId), Account.AccountType.CASH);
  }

  @Override
  @Cacheable(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId")
  public List<AccountDetailsResponse> getUserAccounts(String userId) {
    return accountRepository.findByAppUserId(UUID.fromString(userId))
        .stream()
        .map(accountMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public void debitAccount(String userId, String accountId, Float amount) {
    final var account = this.getAccountEntity(userId, accountId);
    account.debit(amount);
    accountRepository.save(account);
  }

  @Override
  public void creditAccount(String userId, String accountId, Float amount) {
    final var account = this.getAccountEntity(userId, accountId);
    account.credit(amount);
    accountRepository.save(account);
  }
}
