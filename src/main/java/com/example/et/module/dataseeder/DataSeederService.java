package com.example.et.module.dataseeder;

public interface DataSeederService {
  int seedUsers(int count);

  int seedData(int noOfUsers, int monthsOfTransactions);
}
