package com.example.et.module.dashboard.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserSummaryDtoTest {

  @Nested
  @DisplayName("empty()")
  class EmptyTest {
    @Test
    @DisplayName("should create a UserSummaryDto with all fields set to zero")
    void shouldCreateEmptyDtoWithZeros() {
      var dto = UserSummaryDto.empty();

      assertThat(dto.netWorth()).isEqualTo(0.0);
      assertThat(dto.totalIncome()).isEqualTo(0.0);
      assertThat(dto.totalExpense()).isEqualTo(0.0);
      assertThat(dto.netSavings()).isEqualTo(0.0);
      assertThat(dto.dailyBurnRate()).isEqualTo(0.0);
    }
  }

  @Nested
  @DisplayName("getSavingsRate()")
  class SavingsRateTest {

    @Test
    @DisplayName("should calculate savings rate correctly for positive values")
    void shouldCalculateSavingsRate() {
      var dto = new UserSummaryDto(100000.0, 50000.0, 20000.0, 30000.0, 666.67);

      // (50000 - 20000) / 50000 * 100 = 60.0
      assertThat(dto.getSavingsRate()).isEqualTo(60.0f);
    }

    @Test
    @DisplayName("should return NaN when total income is zero")
    void shouldReturnNaNWhenIncomeIsZero() {
      var dto = UserSummaryDto.empty();

      assertThat(dto.getSavingsRate()).isNaN();
    }

    @Test
    @DisplayName("should return negative savings rate when expenses exceed income")
    void shouldCalculateNegativeSavingsRate() {
      var dto = new UserSummaryDto(50000.0, 20000.0, 30000.0, -10000.0, 1000.0);

      // (20000 - 30000) / 20000 * 100 = -50.0
      assertThat(dto.getSavingsRate()).isEqualTo(-50.0f);
    }
  }

  @Nested
  @DisplayName("getFormattedSummary()")
  class FormattedSummaryTest {

    @Test
    @DisplayName("should return correctly formatted summary string")
    void shouldReturnFormattedSummaryString() {
      var dto = new UserSummaryDto(100000.0, 50000.0, 20000.0, 30000.0, 666.67);

      var result = dto.getFormattedSummary();

      assertThat(result).isEqualTo(
          "Total Income: ₹50000.00 | Total Expense: ₹20000.00 | Net Savings: ₹30000.00 (60.00% savings rate)"
      );
    }

    @Test
    @DisplayName("should return formatted summary string with 0.00% when total income is zero")
    void shouldReturnFormattedSummaryStringWhenIncomeIsZero() {
      var dto = UserSummaryDto.empty();

      var result = dto.getFormattedSummary();

      assertThat(result).isEqualTo(
          "Total Income: ₹0.00 | Total Expense: ₹0.00 | Net Savings: ₹0.00 (0.00% savings rate)"
      );
    }
  }
}