package com.example.et.repo;

import com.example.et.model.ai.AiInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiInsightRepo extends JpaRepository<AiInsight, UUID> {
  Optional<AiInsight> findFirstByAppUserIdOrderByCreatedAtDesc(UUID appUserId);

  List<AiInsight> findByAppUserIdOrderByCreatedAtDesc(UUID appUserId);

  long countByAppUserIdAndCreatedAtGreaterThanEqual(UUID appUserId, LocalDateTime after);
}
