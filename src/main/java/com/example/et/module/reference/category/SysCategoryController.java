package com.example.et.module.reference.category;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system-categories")
@RequiredArgsConstructor
public class SysCategoryController {
  private final SysCategoryService sysCategoryService;

  @GetMapping
  public ResponseEntity<List<SystemCategory>> getSystemCategories() {
    return ResponseEntity.ok(sysCategoryService.getAllSystemCategories());
  }
}
