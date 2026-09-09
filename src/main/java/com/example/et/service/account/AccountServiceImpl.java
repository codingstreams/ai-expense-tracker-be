package com.example.et.service.account;

import com.example.et.controller.dto.account.AccountDto;
import com.example.et.controller.dto.account.AccountDtoOld;
import com.example.et.controller.dto.account.UpdateCashDto;
import com.example.et.controller.dto.account.CreateAccountsReq;
import com.example.et.controller.dto.bank.BankDto;
import com.example.et.mapper.AccountMapper;
import com.example.et.mapper.BankMapper;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.BankRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final AccountRepo accountRepo;
  private final BankRepo bankRepo;
  private final AccountMapper accountMapper;
  private final BankMapper bankMapper;

  private static Function<Account, AccountDtoOld> toDto() {
    return account -> new AccountDtoOld(
        account.getId(),
        account.getLastFourDigits(),
        account.getBalance(),
        account.getAccountType(),
        account.getBank(),
        account.isUpiEnabled(),
        account.isNetBankingEnabled()
    );
  }

  @Override
  public List<AccountDtoOld> getUserAccounts(String userId) {
    return accountRepo.findByAppUserId(UUID.fromString(userId))
        .stream().map(toDto())
        .toList();
  }

  @Override
  public List<Account> getUserAccountList(String userId) {
    return accountRepo.findByAppUserId(UUID.fromString(userId));
  }

  @Override
  @CachePut(value = "userAccounts", key = "#userId")
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

    accountRepo.saveAll(accountToBeCreated);

    return getUserAccountsV2(userId);
  }

  @Override
  public Account saveAccount(Account account) {
    return accountRepo.save(account);
  }

  @Override
  public AccountDtoOld getUserAccountDetails(String userId, String accountId) {
    return accountRepo.findByUserIdAndAccountId(UUID.fromString(userId), UUID.fromString(accountId));
  }

  @Override
  @Cacheable(value = "userAccounts", key = "#userId+'_'+#accountId")
  public AccountDto getUserAccountDetailsV2(String userId, String accountId) {
    return accountRepo.findByIdAndAppUserId(UUID.fromString(userId), UUID.fromString(accountId))
        .map(accountMapper::toDto)
        .orElseThrow(() -> new RuntimeException("Account not found."));
  }

  @Override
  @Cacheable(value = "userAccounts", key = "#userId+'_'+#accountId")
  public AccountDto updateAccount(String userId, String accountId, AccountDto account) {
    final var existingAccount = accountRepo.findByIdAndAppUserId(UUID.fromString(accountId), UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    if (account != null) {
      if (account.balance() != null) {
        existingAccount.setBalance(account.balance());
      }
      if (account.lastFourDigits() != null) {
        existingAccount.setLastFourDigits(account.lastFourDigits());
      }
      if (account.accountType() != null) {
        existingAccount.setAccountType(account.accountType());
      }
      // Disable Bank Update
/*      if (account.bank() != null) {
        existingAccount.setBank(account.bank());
      }*/

      existingAccount.setUpiEnabled(Optional.ofNullable(account.upiEnabled()).orElse(existingAccount.isUpiEnabled()));
      existingAccount.setNetBankingEnabled(Optional.ofNullable(account.netBankingEnabled()).orElse(existingAccount.isNetBankingEnabled()));
    }

    final var updatedAccount = accountRepo.save(existingAccount);
    return accountMapper.toDto(updatedAccount);
  }

  @Override
  public AccountDtoOld updateAccount(String userId, String accountId, AccountDtoOld account) {
    final var existingAccount = accountRepo.findByIdAndAppUserId(UUID.fromString(accountId), UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    if (account != null) {
      if (account.balance() != null) {
        existingAccount.setBalance(account.balance());
      }
      if (account.lastFourDigits() != null) {
        existingAccount.setLastFourDigits(account.lastFourDigits());
      }
      if (account.accountType() != null) {
        existingAccount.setAccountType(account.accountType());
      }
      if (account.bank() != null) {
        existingAccount.setBank(account.bank());
      }

      existingAccount.setUpiEnabled(Optional.ofNullable(account.isUpiEnabled()).orElse(existingAccount.isUpiEnabled()));
      existingAccount.setNetBankingEnabled(Optional.ofNullable(account.isNetBankingEnabled()).orElse(existingAccount.isNetBankingEnabled()));
    }

    final var updatedAccount = accountRepo.save(existingAccount);

    return toDto().apply(updatedAccount);
  }

  @Override
  public void deleteAccount(String userId, String accountId) {
    final var existingAccount = accountRepo.findByIdAndAppUserId(UUID.fromString(accountId), UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    existingAccount.setIsActive(false);
    accountRepo.save(existingAccount);
  }

  @Override
  public Account getAccount(UUID userId, UUID accountId) {
    return accountRepo.findByIdAndAppUserId(accountId, userId)
        .orElseThrow(() -> new RuntimeException("Account not found."));
  }

  @Override
  public Float updateCashBalance(String userId, Float cashBalance) {
    final var cashAccount = accountRepo.findCashAccountByUserId(UUID.fromString(userId)).orElseThrow(() -> new RuntimeException("Account not found."));

    if (cashBalance > 0) {
      cashAccount.setBalance(cashBalance);
    }

    accountRepo.save(cashAccount);
    return cashBalance;
  }

  @Override
  @CachePut(value = "userAccounts", key = "#userId")
  public AccountDtoOld updateCashBalance(String userId, UpdateCashDto updateCashDto) {
    final var cashAccount = accountRepo.findCashAccountByUserId(UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    if (updateCashDto.cashBalance() > 0) {
      cashAccount.setBalance(updateCashDto.cashBalance());
    }

    accountRepo.save(cashAccount);
    return toDto().apply(cashAccount);
  }

  @Override
  public List<AccountDtoOld> getUserAccounts(String userId, String paymentMode) {
    return accountRepo.findByAppUserId(UUID.fromString(userId))
        .stream()
        .filter(account -> (account.getAccountType() == Account.AccountType.CASH) || (Objects.nonNull(paymentMode) && paymentMode.toLowerCase().contains("upi")
            ? account.isUpiEnabled()
            : account.isNetBankingEnabled()))
        .map(toDto())
        .toList();
  }

  @Override
  @Cacheable(value = "userAccounts", key = "#userId")
  public AccountDtoOld getUserCashAccountDetails(String userId) {
    return accountRepo.findByUserIdAndAccountType(UUID.fromString(userId), Account.AccountType.CASH);
  }

  @Override
  public Account getUserAccount(String userId, UUID accountId) {
    return accountRepo.findByIdAndAppUserId(accountId, UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));
  }

  @Override
  @Cacheable(value = "userAccounts", key = "#userId")
  public List<AccountDto> getUserAccountsV2(String userId) {
    return accountRepo.findByAppUserId(UUID.fromString(userId))
        .stream()
        .map(accountMapper::toDto)
        .collect(Collectors.toList());
  }
}
