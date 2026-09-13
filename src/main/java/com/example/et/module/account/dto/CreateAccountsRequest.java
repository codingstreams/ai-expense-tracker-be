package com.example.et.module.account.dto;

import java.util.ArrayList;
import java.util.List;

public record CreateAccountsRequest(List<AccountDetailsResponse> accounts) {
  public static CreateAccountsRequest of(ArrayList<AccountDetailsResponse> accounts) {
    return new CreateAccountsRequest(accounts);
  }
}