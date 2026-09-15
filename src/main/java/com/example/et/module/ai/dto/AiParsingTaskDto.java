package com.example.et.module.ai.dto;

import com.example.et.module.ai.parser.AiParsingTask;
import com.example.et.module.transaction.dto.TransactionDto;

import java.util.UUID;

public record AiParsingTaskDto(
    UUID id,
    String rawInput,
    String content,
    String errorMessage,
    UUID correlationId,
    AiParsingTask.Status status,
    TransactionDto transaction
) {
}
