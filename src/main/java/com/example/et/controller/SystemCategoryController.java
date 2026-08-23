package com.example.et.controller;

import com.example.et.model.core.SystemCategory;
import com.example.et.repo.SystemCategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system-categories")
@RequiredArgsConstructor
public class SystemCategoryController {
  private final SystemCategoryRepo systemCategoryRepo;

  @GetMapping
  public ResponseEntity<List<SystemCategory>> getSystemCategories() {
    return ResponseEntity.ok().body(systemCategoryRepo.findAll());
  }
}
