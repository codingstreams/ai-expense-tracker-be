package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UpdateCashDto;
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

  Float updateCashBalance(String userId, Float cashBalance);

  AccountDto updateCashBalance(String userId, UpdateCashDto  updateCashDto);

  List<AccountDto> getUserAccountsV2(String userId, String paymentMode);

  AccountDto getUserCashAccountDetails(String userId);
}
