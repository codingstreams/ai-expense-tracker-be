package com.example.et.controller.dto;

import com.example.et.model.core.Bank;
import com.example.et.model.core.Card;

import java.util.UUID;

public record CardDto(UUID id, Card.CardType cardType, String lastFourDigits, UUID accountId, Float limit, Bank bank) {
}