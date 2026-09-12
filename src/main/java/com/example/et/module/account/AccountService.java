package com.example.et.module.account;

import com.example.et.module.account.dto.AccountDto;
import com.example.et.module.account.dto.AccountDtoOld;
import com.example.et.module.account.dto.CreateAccountsReq;
import com.example.et.module.account.dto.UpdateCashDto;

import java.util.List;
import java.util.UUID;

public interface AccountService {
  List<Account> getUserAccountList(String userId);

  List<AccountDto> addAccounts(String userId, CreateAccountsReq requestBody);

  Account saveAccount(Account account);

  AccountDto getUserAccountDetails(String userId, String accountId);

  AccountDto updateAccount(String userId, String accountId, AccountDto account);

  void deleteAccount(String userId, String accountId);

  Account getAccount(UUID userId, UUID accountId);

  Float updateCashBalance(String userId, Float cashBalance);

  AccountDto updateCashBalance(String userId, UpdateCashDto updateCashDto);

  List<AccountDto> getUserAccounts(String userId, String paymentMode);

  AccountDtoOld getUserCashAccountDetails(String userId);

  Account getUserAccount(String userId, UUID accountId);

  List<AccountDto> getUserAccounts(String userId);
}

