package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UserBankAccounts;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.Bank;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.BankRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
  public void saveAccount(Account account) {
    accountRepo.save(account);
  }

  @Override
  public AccountDto getUserAccountDetails(String userId, String accountId) {
    return accountRepo.findByUserIdAndAccountId(UUID.fromString(userId), UUID.fromString(accountId));
  }
}
