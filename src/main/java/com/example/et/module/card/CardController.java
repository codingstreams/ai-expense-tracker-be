package com.example.et.module.card;


import com.example.et.module.card.dto.CardDto;
import com.example.et.module.card.dto.UserCards;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
  private final CardService cardService;

  @GetMapping
  public ResponseEntity<List<CardDto>> getUserCards(@AuthenticationPrincipal String userId, @RequestParam(name = "type") Card.CardType cardType) {
    return ResponseEntity.ok(cardService.getUserCards(userId, cardType));
  }

  @PostMapping
  public ResponseEntity<List<CardDto>> addCards(@AuthenticationPrincipal String userId, @RequestBody UserCards userCards) {
    return ResponseEntity.ok(cardService.addCards(userId, userCards));
  }
}
