package com.example.et.service.account;

import com.example.et.controller.dto.AccountDto;

import java.util.List;

public interface AccountService {
  List<AccountDto> getUserAccounts(String userId);
}
