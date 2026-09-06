package com.example.et.controller.dto.dashboard;

import com.example.et.controller.dto.account.AccountDtoOld;
import com.example.et.controller.dto.appuser.UpdateUserDetailsDto;

import java.util.List;

public record OnboardUserDto(UpdateUserDetailsDto userConfig, Float cashBalance, List<AccountDtoOld> accounts) {
}