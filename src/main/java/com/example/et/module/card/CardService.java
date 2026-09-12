package com.example.et.module.card;

import com.example.et.module.card.dto.CardDto;
import com.example.et.module.card.dto.UserCards;

import java.util.List;
import java.util.UUID;

public interface CardService {
  List<CardDto> getUserCards(String userId, Card.CardType cardType);

  List<CardDto> addCards(String userId, UserCards userCards);

  Card getUserCard(String userId, UUID cardId);
}
