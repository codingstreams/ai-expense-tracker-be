package com.example.et.module.dataseeder;

import net.datafaker.Faker;

public class ExpenseCatalog {
  private final Faker faker;

  public ExpenseCatalog(Faker faker) {
    this.faker = faker;
  }

  public ExpenseDetail generateExpense(String categoryName) {
    final String cat = categoryName != null ? categoryName.toLowerCase() : "";

    if (cat.contains("grocer")) {
      return new ExpenseDetail(
          faker.options().option("Supermarket Grocery", "Fresh Vegetables & Fruits", "Organic Store", "Hypermarket Run"),
          (float) faker.number().randomDouble(2, 400, 3500)
      );
    }
    if (cat.contains("dining") || cat.contains("food")) {
      return new ExpenseDetail(
          faker.options().option("Dinner with Friends", "Cafe Coffee & Pastry", "Weekend Brunch", "Takeout Order", "Italian Restaurant"),
          (float) faker.number().randomDouble(2, 250, 2200)
      );
    }
    if (cat.contains("fuel") || cat.contains("transport")) {
      return new ExpenseDetail(
          faker.options().option("Petrol Refill", "Uber Ride", "Metro Card Recharge", "Auto Rickshaw Fare"),
          (float) faker.number().randomDouble(2, 150, 1800)
      );
    }
    if (cat.contains("shopping")) {
      return new ExpenseDetail(
          faker.options().option("Apparel & Clothing", "Amazon Online Purchase", "Electronics Accessories", "Home Decor Item"),
          (float) faker.number().randomDouble(2, 800, 6000)
      );
    }
    if (cat.contains("entertainment")) {
      return new ExpenseDetail(
          faker.options().option("Movie Tickets & Popcorn", "Netflix Subscription", "Spotify Premium", "Concert Pass"),
          (float) faker.number().randomDouble(2, 199, 1500)
      );
    }
    if (cat.contains("health") || cat.contains("medical")) {
      return new ExpenseDetail(
          faker.options().option("Pharmacy Medicines", "Doctor Consultation", "Dental Checkup", "Diagnostic Lab Test"),
          (float) faker.number().randomDouble(2, 300, 3000)
      );
    }
    if (cat.contains("invest")) {
      return new ExpenseDetail(
          faker.options().option("Mutual Fund SIP", "Stock Purchase", "Gold Accumulation", "Fixed Deposit Deposit"),
          (float) faker.number().randomDouble(2, 2000, 15000)
      );
    }

    return new ExpenseDetail(
        faker.options().option("Daily Needs Expense", "Local Store Purchase", "Stationery & Supplies", "Courier Delivery"),
        (float) faker.number().randomDouble(2, 100, 1200)
    );
  }

  public record ExpenseDetail(String description, Float amount) {
  }
}
