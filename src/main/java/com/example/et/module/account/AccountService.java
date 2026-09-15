package com.example.et.module.account;

import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.account.dto.AccountDtoOld;
import com.example.et.module.account.dto.CreateAccountsRequest;
import com.example.et.module.account.dto.UpdateCashBalanceRequest;

import java.util.List;

public interface AccountService {
  List<Account> getUserAccountList(String userId);

  List<AccountDetailsResponse> addAccounts(String userId, CreateAccountsRequest requestBody);

  Account saveAccount(Account account);

  AccountDetailsResponse getUserAccountDetails(String userId, String accountId);

  AccountDetailsResponse updateAccount(String userId, String accountId, AccountDetailsResponse account);

  void deleteAccount(String userId, String accountId);

  Account getAccountEntity(String userId, String accountId);

  AccountDetailsResponse getAccount(String userId, String accountId);

  Float updateCashBalance(String userId, Float cashBalance);

  AccountDetailsResponse updateCashBalance(String userId, UpdateCashBalanceRequest updateCashBalanceRequest);

  AccountDtoOld getUserCashAccountDetails(String userId);

  List<AccountDetailsResponse> getUserAccounts(String userId);

  void debitAccount(String userId, String accountId, Float amount);

  void creditAccount(String userId, String accountId, Float amount);
}

