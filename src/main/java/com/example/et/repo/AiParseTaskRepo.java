package com.example.et.repo;

import com.example.et.model.ai.AiParsingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AiParseTaskRepo extends JpaRepository<AiParsingTask, UUID> {
  @Query("SELECT t FROM AiParsingTask t JOIN FETCH t.appUser WHERE t.status = :status ORDER BY t.createdAt ASC")
  List<AiParsingTask> findByStatusWithAppUser(AiParsingTask.Status status);

  List<AiParsingTask> findByStatus(AiParsingTask.Status status);

  @Modifying
  @Query("UPDATE AiParsingTask a SET a.transaction = null WHERE a.transaction.id = :transactionId")
  void unlinkTransaction(UUID transactionId);
}