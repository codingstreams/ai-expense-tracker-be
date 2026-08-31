package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UserBankAccounts;
import com.example.et.model.core.Account;

import java.util.List;

public interface AccountService {
  List<AccountDto> getUserAccounts(String userId);

  List<AccountDto> addAccounts(String userId, UserBankAccounts requestBody);

  void saveAccount(Account account);

  AccountDto getUserAccountDetails(String userId, String accountId);
}
