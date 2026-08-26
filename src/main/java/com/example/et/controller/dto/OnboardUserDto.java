package com.example.et.controller.dto;

import java.util.List;

public record OnboardUserDto(UpdateUserDetailsDto userConfig, List<AccountDto> accounts, Float cashBalance) {
}
