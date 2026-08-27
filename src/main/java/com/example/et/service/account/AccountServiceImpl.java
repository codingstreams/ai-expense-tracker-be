package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UpdateCashDto;
import com.example.et.controller.dto.UserBankAccounts;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.Bank;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.BankRepo;
import com.example.et.repo.PaymentModeRepo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final AccountRepo accountRepo;
  private final PaymentModeRepo paymentModeRepo;
  private final BankRepo bankRepo;

  private static @NonNull Function<Account, AccountDto> toDto() {
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
  public Account getUserAccount(String userId, UUID accountId) {
    return accountRepo.findByIdAndAppUserId(accountId, UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));
  }

  @Override
  public Account saveAccount(Account account) {
    return accountRepo.save(account);
  }

  @Override
  public List<AccountDto> addAccounts(String userId, UserBankAccounts accounts) {
    final var bankIds = accounts.accounts()
        .stream()
        .map(AccountDto::bank)
        .map(Bank::getId)
        .toList();

    if (bankRepo.countByIdIn(bankIds) != bankIds.size()) {
      throw new RuntimeException("Invalid bank ids.");
    }

    final var accountToBeCreated = accounts.accounts()
        .stream()
        .map(accountDto -> Account.builder()
            .appUser(AppUser.ofId(userId))
            .accountType(accountDto.accountType())
            .balance(accountDto.balance())
            .lastFourDigits(accountDto.lastFourDigits())
            .bank(accountDto.bank())
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
  public AccountDto getUserAccountDetails(String userId, String accountId) {
    return accountRepo.findByUserIdAndAccountId(UUID.fromString(userId), UUID.fromString(accountId));
  }

  @Override
  public AccountDto updateAccount(String userId, String accountId, AccountDto accountDto) {
    final var account = accountRepo.findByIdAndAppUserId(UUID.fromString(accountId), UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    account.setBalance(accountDto.balance());
    account.setLastFourDigits(accountDto.lastFourDigits());
    account.setAccountType(accountDto.accountType());
    account.setBank(accountDto.bank());
    account.setUpiEnabled(accountDto.isUpiEnabled());
    account.setNetBankingEnabled(accountDto.isNetBankingEnabled());

    final var updatedAccount = accountRepo.save(account);
    return toDto().apply(updatedAccount);
  }

  @Override
  public void deleteAccount(String userId, String accountId) {
    final var account = accountRepo.findByIdAndAppUserId(UUID.fromString(accountId), UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    account.setIsActive(false);
    accountRepo.save(account);
//    accountRepo.delete(account);
  }

  @Override
  public Float updateCashBalance(String userId, Float cashBalance) {
    final var cashAccount = accountRepo.findCashAccountByUserId(UUID.fromString(userId)).orElseThrow(() -> new RuntimeException("Account not found."));

    if(cashBalance > 0) {
      cashAccount.setBalance(cashBalance);
    }

    accountRepo.save(cashAccount);
    return cashBalance;
  }

  @Override
  public AccountDto getUserCashAccountDetails(String userId) {
    return accountRepo.findByUserIdAndAccountType(UUID.fromString(userId), Account.AccountType.CASH);
  }

  @Override
  public List<AccountDto> getUserAccountsV2(String userId, String paymentMode) {
//final var selectedPaymentMode = paymentModeRepo.findByNameIgnoreCase(paymentMode)
//        .orElseThrow(()->new RuntimeException("Payment mode not found."));

    return accountRepo.findByAppUserId(UUID.fromString(userId))
            .stream()
//            .filter(account->account.isUpiEnabled() || account.isNetBankingEnabled())
            .map(toDto())
            .toList();
  }

  @Override
  public AccountDto updateCashBalance(String userId, UpdateCashDto requestBody) {
    final var cashAccount = accountRepo.findCashAccountByUserId(UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));

    if(requestBody.cashBalance() > 0){
      cashAccount.setBalance(requestBody.cashBalance());
    }

    accountRepo.save(cashAccount);
    return toDto().apply(cashAccount);
  }
}
