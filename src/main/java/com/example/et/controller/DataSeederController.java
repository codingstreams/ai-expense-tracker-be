package com.example.et.controller;

import com.example.et.service.dataseeder.DataSeederService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/generate-data")
@RequiredArgsConstructor
@Profile("dev")
public class DataSeederController {
  private final DataSeederService dataSeederService;

  @PostMapping("/users")
  public ResponseEntity<Void> generateUsers(@RequestParam(defaultValue = "10") Integer usersCount) {
    dataSeederService.seedUsers(usersCount);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/all")
  public ResponseEntity<Map<String, Object>> generateFullData(
      @RequestParam(defaultValue = "5") Integer usersCount,
      @RequestParam(defaultValue = "3") Integer months) {
    int seededCount = dataSeederService.seedData(usersCount, months);
    return ResponseEntity.ok(Map.of(
        "usersSeeded", seededCount,
        "monthsPerUser", months,
        "message", "Data generation completed successfully"
    ));
  }
}

