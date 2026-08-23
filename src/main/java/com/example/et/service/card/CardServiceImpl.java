package com.example.et.service.card;

import com.example.et.controller.dto.CardDto;
import com.example.et.controller.dto.UserCards;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.Card;
import com.example.et.model.core.CardType;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.CardRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
  private final CardRepo cardRepo;
  private final AccountRepo accountRepo;

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
  public List<CardDto> getUserCards(String userId) {
    return cardRepo.findByAppUserId(UUID.fromString(userId))
        .stream()
        .map(toDto())
        .toList();
  }

  @Override
  @Transactional
  public List<CardDto> addCards(String userId, UserCards userCards) {
    final var user = AppUser.ofId(userId);
    final var cardsToSave = userCards.cards().stream().map(cardDto -> {
      Account account;
      if (cardDto.cardType() == CardType.CREDIT_CARD) {
        account = accountRepo.save(Account.builder()
            .appUser(user)
            .accountType(Account.AccountType.CREDIT)
            .balance(cardDto.limit() != null ? cardDto.limit() : 0)
            .lastFourDigits(cardDto.lastFourDigits())
            .bank(cardDto.bank())
            .build());
      } else {
        account = accountRepo.findByIdAndAppUserId(cardDto.accountId(), UUID.fromString(userId))
            .orElseThrow(() -> new RuntimeException("Account not found."));
      }

      return Card.builder()
          .appUser(user)
          .account(account)
          .cardType(cardDto.cardType())
          .lastFourDigits(cardDto.lastFourDigits())
          .build();
    }).toList();

    return cardRepo.saveAll(cardsToSave)
        .stream()
        .map(toDto())
        .toList();
  }
}
