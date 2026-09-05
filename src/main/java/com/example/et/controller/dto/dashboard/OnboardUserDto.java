package com.example.et.controller.dto.dashboard;

import com.example.et.controller.dto.account.AccountDto;
import com.example.et.controller.dto.appuser.UpdateUserDetailsDto;

import java.util.List;

public record OnboardUserDto(UpdateUserDetailsDto userConfig, Float cashBalance, List<AccountDto> accounts) {
}