package com.example.et.controller.dto;

import java.util.List;

public record OnboardUserDto(UpdateUserDetailsDto userConfig, Float cashBalance, List<AccountDto> accounts) {
}