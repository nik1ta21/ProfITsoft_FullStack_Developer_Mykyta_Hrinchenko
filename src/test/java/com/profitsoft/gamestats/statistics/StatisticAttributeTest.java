package com.profitsoft.gamestats.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StatisticAttributeTest {

  @Test
  void findsAttributeByItsName() {
    // when / then
    assertEquals(StatisticAttribute.DEVELOPER, StatisticAttribute.fromName("developer"));
    assertEquals(StatisticAttribute.YEAR_RELEASED, StatisticAttribute.fromName("year_released"));
    assertEquals(StatisticAttribute.GENRE, StatisticAttribute.fromName("genre"));
  }

  @Test
  void rejectsUnknownAttributeAndListsSupportedOnes() {
    // when
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> StatisticAttribute.fromName("title"));

    // then
    assertTrue(exception.getMessage().contains("developer, year_released, genre"));
  }

  @Test
  void rejectsNullAttributeName() {
    // when / then
    assertThrows(IllegalArgumentException.class, () -> StatisticAttribute.fromName(null));
  }
}
