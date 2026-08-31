package com.example.et.service.card;

import com.example.et.controller.dto.CardDto;
import com.example.et.model.core.Card;

import java.util.List;

public interface CardService {
  List<CardDto> getUserCards(String userId, Card.CardType cardType);
}
