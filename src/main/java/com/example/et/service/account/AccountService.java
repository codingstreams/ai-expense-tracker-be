package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;
import com.example.et.controller.dto.UserBankAccounts;
import com.example.et.model.core.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
  List<AccountDto> getUserAccounts(String userId);
  Account getUserAccount(UUID accountId);

  List<AccountDto> addAccounts(String userId, UserBankAccounts accounts);
}
