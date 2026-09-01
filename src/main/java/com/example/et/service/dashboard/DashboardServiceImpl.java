package com.example.et.service.dashboard;

import com.example.et.controller.dto.OnboardUserDto;
import com.example.et.controller.dto.UserBankAccounts;
import com.example.et.service.account.AccountService;
import com.example.et.service.appuser.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
  private final AppUserService appUserService;
  private final AccountService accountService;

  @Override
  public OnboardUserDto onboardUser(String userId, OnboardUserDto requestBody) {
    final var accounts = accountService.addAccounts(userId, new UserBankAccounts(requestBody.accounts()));
    final var cashBalance = accountService.updateCashBalance(userId, requestBody.cashBalance());
    final var userConfig = appUserService.updateUserConfig(userId, requestBody.userConfig());

    return new OnboardUserDto(userConfig, cashBalance, accounts);
  }
}
