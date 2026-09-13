package com.example.et.module.account.dto;

import java.util.ArrayList;
import java.util.List;

public record CreateAccountsRequest(List<AccountDto> accounts) {
  public static CreateAccountsRequest of(ArrayList<AccountDto> accounts) {
    return new CreateAccountsRequest(accounts);
  }
}