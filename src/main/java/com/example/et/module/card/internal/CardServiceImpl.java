package com.example.et.module.card.internal;

import com.example.et.core.cache.CacheNames;
import com.example.et.module.account.Account;
import com.example.et.module.account.AccountService;
import com.example.et.module.auth.AppUser;
import com.example.et.module.card.Card;
import com.example.et.module.card.CardMapper;
import com.example.et.module.card.CardService;
import com.example.et.module.card.dto.CardDto;
import com.example.et.module.card.dto.UserCards;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
  private final CardRepo cardRepo;
  private final AccountService accountService;
  private final CardMapper cardMapper;

  @Override
  @Cacheable(
      value = CacheNames.USER_CARDS,
      key = "#userId + ':' + (#cardType != null ? #cardType : 'ALL')",
      unless = "#result == null || #result.isEmpty()"
  )
  public List<CardDto> getUserCards(String userId, Card.CardType cardType) {
    return cardRepo.findByAppUserId(UUID.fromString(userId))
        .stream()
        .filter(card -> cardType == null || card.getCardType() == cardType)
        .map(cardMapper::toDto)
        .toList();
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + ':ALL'"),
      @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + ':CREDIT_CARD'"),
      @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + ':DEBIT_CARD'"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId")
  })
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
        account = accountService.getAccount(UUID.fromString(userId), cardDto.accountId());
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
        .map(cardMapper::toDto)
        .toList();
  }

  @Override
  @Cacheable(value = CacheNames.USER_CARDS, key = "#userId + ':card:' + #cardId")
  public Card getUserCard(String userId, UUID cardId) {
    return cardRepo.findByIdAndAppUserId(cardId, UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Card not found."));
  }

}
