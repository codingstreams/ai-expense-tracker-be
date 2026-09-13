package com.example.et.module.card.dto;

import com.example.et.module.card.Card;
import com.example.et.module.reference.bank.Bank;

import java.util.UUID;

public record CardResponse(UUID id, Card.CardType cardType, String lastFourDigits, UUID accountId, Float limit,
                           Bank bank) {
}