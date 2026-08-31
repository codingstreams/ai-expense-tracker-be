package com.example.et.service.card;

import com.example.et.controller.dto.CardDto;
import com.example.et.model.core.Card;
import com.example.et.repo.CardRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
  private final CardRepo cardRepo;

  private static Function<Card, CardDto> toDto() {
    return card -> new CardDto(
        card.getId(),
        card.getCardType(),
        card.getLastFourDigits(),
        card.getAccount() != null ? card.getAccount().getId() : null,
        card.getAccount() != null ? card.getAccount().getBalance() : null,
        card.getAccount() != null ? card.getAccount().getBank() : null
    );
  }

  @Override
  public List<CardDto> getUserCards(String userId, Card.CardType cardType) {
    return cardRepo.findByAppUserId(UUID.fromString(userId))
        .stream()
        .filter(card -> cardType == null || card.getCardType() == cardType)
        .map(toDto())
        .toList();
  }

}
