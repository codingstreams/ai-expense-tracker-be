package com.example.et.module.account.internal;

import com.example.et.core.cache.CacheNames;
import com.example.et.module.account.Account;
import com.example.et.module.account.AccountMapper;
import com.example.et.module.account.AccountService;
import com.example.et.module.account.dto.AccountDto;
import com.example.et.module.account.dto.AccountDtoOld;
import com.example.et.module.account.dto.CreateAccountsReq;
import com.example.et.module.account.dto.UpdateCashDto;
import com.example.et.module.auth.AppUser;
import com.example.et.module.reference.bank.BankMapper;
import com.example.et.module.reference.bank.dto.BankDto;
import com.example.et.module.reference.bank.internal.BankRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final AccountRepo accountRepo;
  private final BankRepo bankRepo;
  private final AccountMapper accountMapper;
  private final BankMapper bankMapper;

  @Override
  public List<Account> getUserAccountList(String userId) {
    return accountRepo.findByAppUserId(UUID.fromString(userId));
  }

  @Override
  @Caching(
      evict = {
          @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
          @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
      }
  )
  public List<AccountDto> addAccounts(String userId, CreateAccountsReq requestBody) {
    final var bankIds = requestBody.accounts()
        .stream()
        .map(AccountDto::bank)
        .map(BankDto::id)
        .collect(Collectors.toSet());

    if (bankRepo.countByIdIn(bankIds) != bankIds.size()) {
      throw new RuntimeException("Invalid bank ids.");
    }

    final var accountToBeCreated = requestBody.accounts()
        .stream()
        .map(accountDto -> Account.builder()
            .appUser(AppUser.ofId(userId))
            .accountType(accountDto.accountType())
            .balance(accountDto.balance())
            .lastFourDigits(accountDto.lastFourDigits())
            .bank(bankMapper.toEntity(accountDto.bank()))
            .isActive(true)
            .upiEnabled(Optional.ofNullable(accountDto.upiEnabled()).orElse(true))
            .netBankingEnabled(Optional.ofNullable(accountDto.netBankingEnabled()).orElse(true))
            .build())
        .toList();

    return accountRepo.saveAll(accountToBeCreated)
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
    return accountRepo.save(account);
  }

  @Override
  @Cacheable(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, #accountId}")
  public AccountDto getUserAccountDetails(String userId, String accountId) {
    return accountRepo.findByUserIdAndAccountId(UUID.fromString(userId), UUID.fromString(accountId))
        .map(accountMapper::toDto)
        .orElseThrow();
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, #accountId}"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
  })
  public AccountDto updateAccount(String userId, String accountId, AccountDto account) {
    final var existingAccount = accountRepo.findByIdAndAppUserId(UUID.fromString(accountId), UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

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

    final var updatedAccount = isUpdateRequired ? accountRepo.save(existingAccount) : existingAccount;
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
    final var existingAccount = accountRepo.findByIdAndAppUserId(UUID.fromString(accountId), UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    if (existingAccount.getAccountType().compareTo(Account.AccountType.CASH) == 0) {
      throw new RuntimeException("Cannot delete cash account");
    }

    existingAccount.setIsActive(false);
    accountRepo.save(existingAccount);
  }

  @Override
  public Account getAccount(UUID userId, UUID accountId) {
    return accountRepo.findByIdAndAppUserId(accountId, userId)
        .orElseThrow(() -> new RuntimeException("Account not found."));
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
  })
  public Float updateCashBalance(String userId, Float cashBalance) {
    final var cashAccount = accountRepo.findCashAccountByUserId(UUID.fromString(userId)).orElseThrow(() -> new RuntimeException("Account not found."));

    if (cashBalance > 0) {
      cashAccount.setBalance(cashBalance);
    }

    accountRepo.save(cashAccount);
    return cashBalance;
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}"),
      @CacheEvict(value = CacheNames.USER_FINANCIAL_SUMMARY, allEntries = true)
  })
  public AccountDto updateCashBalance(String userId, UpdateCashDto updateCashDto) {
    final var cashAccount = accountRepo.findCashAccountByUserId(UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    if (updateCashDto.cashBalance() > 0) {
      cashAccount.setBalance(updateCashDto.cashBalance());
    }

    return accountMapper.toDto(accountRepo.save(cashAccount));
  }

  @Override
  @Cacheable(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId")
  public List<AccountDto> getUserAccounts(String userId, String paymentMode) {
    return accountRepo.findByAppUserId(UUID.fromString(userId))
        .stream()
        .filter(account -> (account.getAccountType() == Account.AccountType.CASH) || (Objects.nonNull(paymentMode) && paymentMode.toLowerCase().contains("upi")
            ? account.isUpiEnabled()
            : account.isNetBankingEnabled()))
        .map(accountMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Cacheable(value = CacheNames.USER_BANK_ACCOUNTS, key = "{#userId, 'cash'}")
  public AccountDtoOld getUserCashAccountDetails(String userId) {
    return accountRepo.findByUserIdAndAccountType(UUID.fromString(userId), Account.AccountType.CASH);
  }

  @Override
  public Account getUserAccount(String userId, UUID accountId) {
    return accountRepo.findByIdAndAppUserId(accountId, UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));
  }

  @Override
  @Cacheable(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId")
  public List<AccountDto> getUserAccounts(String userId) {
    return accountRepo.findByAppUserId(UUID.fromString(userId))
        .stream()
        .map(accountMapper::toDto)
        .collect(Collectors.toList());
  }
}
