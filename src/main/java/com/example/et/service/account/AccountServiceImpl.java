package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UpdateCashDto;
import com.example.et.controller.dto.UserBankAccounts;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.Bank;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.BankRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final AccountRepo accountRepo;
  private final BankRepo bankRepo;

  private static Function<Account, AccountDto> toDto() {
    return account -> new AccountDto(
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
  public List<AccountDto> getUserAccounts(String userId) {
    return accountRepo.findByAppUserId(UUID.fromString(userId))
        .stream().map(toDto())
        .toList();
  }

  @Override
  public List<Account> getUserAccountList(String userId) {
    return accountRepo.findByAppUserId(UUID.fromString(userId));
  }

  @Override
  public List<AccountDto> addAccounts(String userId, UserBankAccounts requestBody) {
    final var bankIds = requestBody.accounts()
        .stream()
        .map(AccountDto::bank)
        .map(Bank::getId)
        .toList();

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
            .bank(accountDto.bank())
            .isActive(true)
            .isUpiEnabled(Optional.ofNullable(accountDto.isUpiEnabled()).orElse(true))
            .isNetBankingEnabled(Optional.ofNullable(accountDto.isNetBankingEnabled()).orElse(true))
            .build())
        .toList();

    final var accountsCreated = accountRepo.saveAll(accountToBeCreated);

    return accountsCreated.stream()
        .map(toDto())
        .toList();
  }

  @Override
  public Account saveAccount(Account account) {
    return accountRepo.save(account);
  }

  @Override
  public AccountDto getUserAccountDetails(String userId, String accountId) {
    return accountRepo.findByUserIdAndAccountId(UUID.fromString(userId), UUID.fromString(accountId));
  }

  @Override
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
  public AccountDto updateCashBalance(String userId, UpdateCashDto updateCashDto) {
    final var cashAccount = accountRepo.findCashAccountByUserId(UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    if (updateCashDto.cashBalance() > 0) {
      cashAccount.setBalance(updateCashDto.cashBalance());
    }

    accountRepo.save(cashAccount);
    return toDto().apply(cashAccount);
  }

  @Override
  public List<AccountDto> getUserAccountsV2(String userId, String paymentMode) {
    return accountRepo.findByAppUserId(UUID.fromString(userId))
        .stream()
        .filter(account -> (account.getAccountType() == Account.AccountType.CASH) || (Objects.nonNull(paymentMode) && paymentMode.toLowerCase().contains("upi")
            ? account.isUpiEnabled()
            : account.isNetBankingEnabled()))
        .map(toDto())
        .toList();
  }

  @Override
  public AccountDto getUserCashAccountDetails(String userId) {
    return accountRepo.findByUserIdAndAccountType(UUID.fromString(userId), Account.AccountType.CASH);
  }

  @Override
  public Account getUserAccount(String userId, UUID accountId) {
    return accountRepo.findByIdAndAppUserId(accountId, UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));
  }
}
