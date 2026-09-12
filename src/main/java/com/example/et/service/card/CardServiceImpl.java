package com.example.et.service.card;

import com.example.et.config.CacheConfig;
import com.example.et.controller.dto.card.CardDto;
import com.example.et.controller.dto.card.UserCards;
import com.example.et.mapper.CardMapper;
import com.example.et.model.core.Account;
import com.example.et.model.core.AppUser;
import com.example.et.model.core.Card;
import com.example.et.repo.card.CardRepo;
import com.example.et.service.account.AccountService;
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
      value = CacheConfig.USER_CARDS_CACHE,
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
      @CacheEvict(value = CacheConfig.USER_CARDS_CACHE, key = "#userId + ':ALL'"),
      @CacheEvict(value = CacheConfig.USER_CARDS_CACHE, key = "#userId + ':CREDIT_CARD'"),
      @CacheEvict(value = CacheConfig.USER_CARDS_CACHE, key = "#userId + ':DEBIT_CARD'"),
      @CacheEvict(value = CacheConfig.USER_BANK_ACCOUNTS_CACHE, key = "#userId")
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
  @Cacheable(value = CacheConfig.USER_CARDS_CACHE, key = "#userId + ':card:' + #cardId")
  public Card getUserCard(String userId, UUID cardId) {
    return cardRepo.findByIdAndAppUserId(cardId, UUID.fromString(userId))
        .orElseThrow(() -> new RuntimeException("Card not found."));
  }

}
