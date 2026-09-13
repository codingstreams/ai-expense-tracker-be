package com.example.et.module.card;


import com.example.et.module.card.dto.AddCardsRequest;
import com.example.et.module.card.dto.CardsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
  private final CardService cardService;

  @GetMapping
  public ResponseEntity<CardsResponse> getUserCards(@AuthenticationPrincipal String userId, @RequestParam(name = "type") Card.CardType cardType) {
    return ResponseEntity.ok(cardService.getUserCards(userId, cardType));
  }

  @PostMapping
  public ResponseEntity<CardsResponse> addCards(@AuthenticationPrincipal String userId, @RequestBody AddCardsRequest addCardsRequest) {
    return ResponseEntity.ok(cardService.addCards(userId, addCardsRequest));
  }
}
