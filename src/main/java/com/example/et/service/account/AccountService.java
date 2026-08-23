package com.example.et.service.account;

import com.example.et.model.core.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
  List<Account> getUserAccounts(UUID userId);
  Account getUserAccount(UUID accountId);
}
