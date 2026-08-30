package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;
import com.example.et.model.core.Account;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.BankRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
}
