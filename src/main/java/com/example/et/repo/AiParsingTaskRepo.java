package com.example.et.repo;

import com.example.et.model.ai.AiParsingTask;
import com.example.et.model.ai.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AiParsingTaskRepo extends JpaRepository<AiParsingTask, UUID> {
  @Query("SELECT t FROM AiParsingTask t JOIN FETCH t.appUser WHERE t.status = :status ORDER BY t.createdAt ASC")
  List<AiParsingTask> findByStatusWithAppUser(Status status);

  List<AiParsingTask> findByStatus(Status status);

  @Modifying
  @Query("UPDATE AiParsingTask a SET a.transaction = null WHERE a.transaction.id = :transactionId")
  void unlinkTransaction(UUID transactionId);
}
