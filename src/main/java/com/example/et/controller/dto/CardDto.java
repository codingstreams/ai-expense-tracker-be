package com.example.et.controller.dto;

import com.example.et.model.core.Bank;
import com.example.et.model.core.CardType;

import java.math.BigDecimal;
import java.util.UUID;

public record CardDto(UUID id, CardType cardType, String lastFourDigits, UUID accountId, BigDecimal limit, Bank bank) {
}
