package com.example.et.service.card;

import com.example.et.controller.dto.CardDto;
import com.example.et.controller.dto.UserCards;
import com.example.et.model.core.Card;
import com.example.et.model.core.CardType;

import java.util.List;
import java.util.UUID;

public interface CardService {
  List<CardDto> getUserCards(String userId, CardType cardType);

  Card getUserCard(String userId, UUID cardId);

  List<CardDto> addCards(String userId, UserCards userCards);
}
