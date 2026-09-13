package com.example.et.module.account;

import com.example.et.module.account.dto.AccountDto;
import com.example.et.module.account.dto.AccountDtoOld;
import com.example.et.module.account.dto.CreateAccountsReq;
import com.example.et.module.account.dto.UpdateCashDto;

import java.util.List;

public interface AccountService {
  List<Account> getUserAccountList(String userId);

  List<AccountDto> addAccounts(String userId, CreateAccountsReq requestBody);

  Account saveAccount(Account account);

  AccountDto getUserAccountDetails(String userId, String accountId);

  AccountDto updateAccount(String userId, String accountId, AccountDto account);

  void deleteAccount(String userId, String accountId);

  Account getAccountEntity(String userId, String accountId);

  AccountDto getAccount(String userId, String accountId);

  Float updateCashBalance(String userId, Float cashBalance);

  AccountDto updateCashBalance(String userId, UpdateCashDto updateCashDto);

  AccountDtoOld getUserCashAccountDetails(String userId);

  List<AccountDto> getUserAccounts(String userId);
}

