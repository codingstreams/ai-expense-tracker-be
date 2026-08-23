package com.example.et.service.card;

import com.example.et.controller.dto.CardDto;
import com.example.et.controller.dto.UserCards;

import java.util.List;

public interface CardService {
  List<CardDto> getUserCards(String userId);

  List<CardDto> addCards(String userId, UserCards userCards);
}
