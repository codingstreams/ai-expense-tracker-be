package com.example.et.service.card;

import com.example.et.controller.dto.CardDto;
import com.example.et.controller.dto.UserCards;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.Card;
import com.example.et.repo.CardRepo;
import com.example.et.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
  private final CardRepo cardRepo;
  private final AccountService accountService;

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

  @Override
  public List<CardDto> addCards(String userId, UserCards userCards) {
    final var user = AppUser.ofId(userId);

    final var cardsToSave = userCards.cards().stream().map(cardDto -> {
      Account account;

      if (cardDto.cardType() == Card.CardType.CREDIT_CARD) {
        account = accountService.saveAccount(Account.builder()
            .appUser(user)
            .accountType(Account.AccountType.CREDIT)
            .balance(cardDto.limit() != null ? cardDto.limit() : 0.0f)
            .lastFourDigits(cardDto.lastFourDigits())
                .isActive(true)
            .bank(cardDto.bank())
            .build());
      } else {
        account = accountService.getAccount(cardDto.accountId(), UUID.fromString(userId));
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

  @Override
  public Card getUserCard(String userId, UUID cardId) {
    return cardRepo.findByIdAndAppUserId(cardId, UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Card not found."));
  }

}
