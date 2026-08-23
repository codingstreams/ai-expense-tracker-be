package com.example.et.service.account;

import com.example.et.model.core.Account;
import com.example.et.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements  AccountService{

  private final AccountRepo accountRepo;

  @Override
  public List<Account> getUserAccounts(UUID userId) {
    return accountRepo.findByAppUserId(userId);
  }

  @Override
  public Account getUserAccount(UUID accountId) {
    return null;
  }
}
