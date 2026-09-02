package com.example.et.service.card;

import com.example.et.controller.dto.CardDto;
import com.example.et.controller.dto.UserCards;
import com.example.et.model.core.Card;

import java.util.List;
import java.util.UUID;

public interface CardService {
  List<CardDto> getUserCards(String userId, Card.CardType cardType);

  List<CardDto> addCards(String userId, UserCards userCards);
  Card getUserCard(String userId, UUID cardId);
}
