package com.example.et.repo;

import com.example.et.model.core.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepo extends JpaRepository<Card, UUID> {
  @Query("select c from Card c left join fetch c.account a left join fetch a.bank where c.appUser.id = :userId")
  List<Card> findByAppUserId(UUID userId);

  @Query("select c from Card c left join fetch c.account where c.id = :id and c.appUser.id = :appUserId")
  Optional<Card> findByIdAndAppUserId(UUID id, UUID appUserId);
}
