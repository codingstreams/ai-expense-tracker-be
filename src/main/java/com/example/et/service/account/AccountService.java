package com.example.et.service.account;

import com.example.et.controller.dto.account.AccountDto;
import com.example.et.controller.dto.account.AccountDtoOld;
import com.example.et.controller.dto.account.UpdateCashDto;
import com.example.et.controller.dto.account.UserBankAccounts;
import com.example.et.model.core.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
  List<AccountDtoOld> getUserAccounts(String userId);

  List<Account> getUserAccountList(String userId);

  List<AccountDtoOld> addAccounts(String userId, UserBankAccounts requestBody);

  Account saveAccount(Account account);

  AccountDtoOld getUserAccountDetails(String userId, String accountId);

  AccountDtoOld updateAccount(String userId, String accountId, AccountDtoOld account);

  void deleteAccount(String userId, String accountId);

  Account getAccount(UUID userId, UUID accountId);

  Float updateCashBalance(String userId, Float cashBalance);

  AccountDtoOld updateCashBalance(String userId, UpdateCashDto  updateCashDto);

  List<AccountDtoOld> getUserAccountsV2(String userId, String paymentMode);

  AccountDtoOld getUserCashAccountDetails(String userId);

  Account getUserAccount(String userId, UUID accountId);

  List<AccountDto> getUserAccountsV3(String userId);
}

