package com.example.et.module.card.internal;

import com.example.et.core.config.CacheNames;
import com.example.et.module.account.Account;
import com.example.et.module.account.AccountMapper;
import com.example.et.module.account.AccountService;
import com.example.et.module.card.Card;
import com.example.et.module.card.CardMapper;
import com.example.et.module.card.CardService;
import com.example.et.module.card.dto.AddCardsRequest;
import com.example.et.module.card.dto.CardsResponse;
import com.example.et.module.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
  private final CardRepo cardRepo;
  private final AccountService accountService;
  private final CardMapper cardMapper;
  private final AccountMapper accountMapper;

  @Override
  @Cacheable(
      value = CacheNames.USER_CARDS,
      key = "#userId + ':' + (#cardType != null ? #cardType : 'ALL')",
      unless = "#result == null || #result.cards().isEmpty()"
  )
  public CardsResponse getUserCards(String userId, Card.CardType cardType) {
    final var cards = cardRepo.findByAppUserId(UUID.fromString(userId))
        .stream()
        .filter(card -> cardType == null || card.getCardType() == cardType)
        .map(cardMapper::toDto)
        .collect(Collectors.toList());

    return new CardsResponse(cards);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + ':ALL'"),
      @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + ':CREDIT_CARD'"),
      @CacheEvict(value = CacheNames.USER_CARDS, key = "#userId + ':DEBIT_CARD'"),
      @CacheEvict(value = CacheNames.USER_BANK_ACCOUNTS, key = "#userId")
  })
  public CardsResponse addCards(String userId, AddCardsRequest addCardsRequest) {
    final var user = AppUser.ofId(userId);

    final var cardsToSave = addCardsRequest.cards().stream().map(cardDto -> {
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
        account = accountMapper.toEntity(accountService.getAccount(userId, cardDto.accountId()), userId);
      }

      return Card.builder()
          .appUser(user)
          .account(account)
          .cardType(cardDto.cardType())
          .lastFourDigits(cardDto.lastFourDigits())
          .build();
    }).toList();

    final var cards = cardRepo.saveAll(cardsToSave)
        .stream()
        .map(cardMapper::toDto)
        .collect(Collectors.toList());

    return new CardsResponse(cards);
  }

  @Override
  @Cacheable(value = CacheNames.USER_CARDS, key = "#userId + ':card:' + #cardId")
  public Card getUserCard(String userId, UUID cardId) {
    return cardRepo.findByIdAndAppUserId(cardId, UUID.fromString(userId))
        .orElseThrow(() -> new com.example.et.core.exception.ApiException(com.example.et.core.exception.ErrorCode.CARD_NOT_FOUND));
  }

}
