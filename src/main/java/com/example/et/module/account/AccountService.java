package com.example.et.module.account;

import com.example.et.module.account.dto.AccountDto;
import com.example.et.module.account.dto.AccountDtoOld;
import com.example.et.module.account.dto.CreateAccountsRequest;
import com.example.et.module.account.dto.UpdateCashBalanceRequest;

import java.util.List;

public interface AccountService {
  List<Account> getUserAccountList(String userId);

  List<AccountDto> addAccounts(String userId, CreateAccountsRequest requestBody);

  Account saveAccount(Account account);

  AccountDto getUserAccountDetails(String userId, String accountId);

  AccountDto updateAccount(String userId, String accountId, AccountDto account);

  void deleteAccount(String userId, String accountId);

  Account getAccountEntity(String userId, String accountId);

  AccountDto getAccount(String userId, String accountId);

  Float updateCashBalance(String userId, Float cashBalance);

  AccountDto updateCashBalance(String userId, UpdateCashBalanceRequest updateCashBalanceRequest);

  AccountDtoOld getUserCashAccountDetails(String userId);

  List<AccountDto> getUserAccounts(String userId);

  void debitAccount(String userId, String accountId, Float amount);

  void creditAccount(String userId, String accountId, Float amount);
}

