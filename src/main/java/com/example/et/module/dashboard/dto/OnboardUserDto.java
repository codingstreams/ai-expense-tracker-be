package com.example.et.module.dashboard.dto;

import com.example.et.module.account.dto.AccountDetailsResponse;
import com.example.et.module.user.dto.UpdateUserDetailsDto;

import java.util.List;

public record OnboardUserDto(UpdateUserDetailsDto userConfig, Float cashBalance,
                             List<AccountDetailsResponse> accounts) {
}