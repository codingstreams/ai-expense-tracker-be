package com.example.et.controller.dto;

import java.util.ArrayList;
import java.util.List;

public record UserBankAccounts(List<AccountDto> accounts) {
  public static UserBankAccounts of(ArrayList<AccountDto> accounts) {
    return new UserBankAccounts(accounts);
  }
}