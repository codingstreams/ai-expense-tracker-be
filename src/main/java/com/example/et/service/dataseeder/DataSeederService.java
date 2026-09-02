package com.example.et.service.dataseeder;

public interface DataSeederService {
  int seedUsers(int count);
  int seedData(int noOfUsers, int monthsOfTransactions);
}
