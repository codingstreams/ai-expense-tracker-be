package com.example.et.module.card;

import com.example.et.module.card.dto.AddCardsRequest;
import com.example.et.module.card.dto.CardsResponse;

import java.util.UUID;

public interface CardService {
  CardsResponse getUserCards(String userId, Card.CardType cardType);

  CardsResponse addCards(String userId, AddCardsRequest addCardsRequest);

  Card getUserCard(String userId, UUID cardId);
}
