package com.example.et.controller.dto.account;

import java.util.ArrayList;
import java.util.List;

public record CreateAccountsReq(List<AccountDto> accounts) {
  public static CreateAccountsReq of(ArrayList<AccountDto> accounts) {
    return new CreateAccountsReq(accounts);
  }
}