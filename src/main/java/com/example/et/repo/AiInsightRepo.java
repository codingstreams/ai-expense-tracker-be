package com.example.et.repo;

import aj.org.objectweb.asm.commons.Remapper;
import com.example.et.model.ai.AiInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiInsightRepo extends JpaRepository<AiInsight, UUID> {
  Optional<AiInsight> findFirstByAppUserIdOrderByCreatedAtDesc(UUID userId);
}