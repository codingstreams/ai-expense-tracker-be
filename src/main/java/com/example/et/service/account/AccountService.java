package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UserBankAccounts;
import com.example.et.model.core.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
  List<AccountDto> getUserAccounts(String userId);

  List<Account> getUserAccountList(String userId);

  List<AccountDto> addAccounts(String userId, UserBankAccounts requestBody);

  Account saveAccount(Account account);

  AccountDto getUserAccountDetails(String userId, String accountId);

  AccountDto updateAccount(String userId, String accountId, AccountDto account);

  void deleteAccount(String userId, String accountId);

  Account getAccount(UUID userId, UUID accountId);
}
