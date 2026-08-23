package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UserBankAccounts;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.Bank;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.BankRepo;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
  private final AccountRepo accountRepo;
  private final BankRepo bankRepo;

  private static @NonNull Function<Account, AccountDto> toDto() {
    return account -> new AccountDto(account.getId(), account.getLastFourDigits(), account.getBalance(), account.getAccountType(), account.getBank());
  }

  @Override
  public List<AccountDto> getUserAccounts(String userId) {
    return accountRepo.findByAppUserId(UUID.fromString(userId))
        .stream().map(toDto())
        .toList();
  }

  @Override
  public Account getUserAccount(UUID accountId) {
    return null;
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

    // check for null before updating
    account.setBalance(accountDto.balance());
    account.setLastFourDigits(accountDto.lastFourDigits());
    account.setAccountType(accountDto.accountType());
    account.setBank(accountDto.bank());

    final var updatedAccount = accountRepo.save(account);
    return toDto().apply(updatedAccount);
  }

  // we can also use soft delete -> first just inactive the account then delete it later
  @Override
  public void deleteAccount(String userId, String accountId) {
    final var account = accountRepo.findByIdAndAppUserId(UUID.fromString(accountId), UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Account not found."));
    accountRepo.delete(account);
  }
}
