package com.example.et.controller.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TransactionFilterParams(
    String type,
    String category,
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate,
    
    Float minAmount,
    Float maxAmount
) {}