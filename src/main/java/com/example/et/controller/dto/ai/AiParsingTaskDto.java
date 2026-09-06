package com.example.et.controller.dto.ai;

import com.example.et.controller.dto.transaction.TransactionDto;
import com.example.et.model.ai.AiParsingTask;

import java.util.UUID;

public record AiParsingTaskDto(
    UUID id,
    String rawInput,
    String content,
    String errorMessage,
    UUID correlationId,
    AiParsingTask.Status status,
    TransactionDto transaction
) {}
