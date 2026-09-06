package com.example.et.controller.dto.account;

import java.util.ArrayList;
import java.util.List;

public record UserBankAccounts(List<AccountDtoOld> accounts) {
  public static UserBankAccounts of(ArrayList<AccountDtoOld> accounts) {
    return new UserBankAccounts(accounts);
  }
}