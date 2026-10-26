package com.profitsoft.gamestats.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.profitsoft.gamestats.model.Developer;
import com.profitsoft.gamestats.model.Game;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatisticsCalculatorTest {

  @Test
  void countsGamesByDeveloper() {
    // given
    StatisticsCalculator calculator = calculatorWith(StatisticAttribute.DEVELOPER,
        game("D1", 2000, "RPG"), game("D1", 2001, "RPG"), game("D2", 2002, "RPG"));

    // when
    List<StatisticItem> statistics = calculator.getSortedStatistics();

    // then
    assertEquals(List.of(new StatisticItem("D1", 2), new StatisticItem("D2", 1)), statistics);
  }

  @Test
  void countsGamesByYear() {
    // given
    StatisticsCalculator calculator = calculatorWith(StatisticAttribute.YEAR_RELEASED,
        game("D1", 2000, "RPG"), game("D2", 2000, "RPG"), game("D3", 2001, "RPG"));

    // when
    List<StatisticItem> statistics = calculator.getSortedStatistics();

    // then
    assertEquals(List.of(new StatisticItem("2000", 2), new StatisticItem("2001", 1)), statistics);
  }

  @Test
  void countsEveryGenreOfGameSeparately() {
    // given
    StatisticsCalculator calculator = calculatorWith(StatisticAttribute.GENRE,
        game("D1", 2000, "RPG", "Action"), game("D2", 2001, "RPG", "Adventure"),
        game("D3", 2002, "Strategy"));

    // when
    List<StatisticItem> statistics = calculator.getSortedStatistics();

    // then
    assertEquals(List.of(new StatisticItem("RPG", 2), new StatisticItem("Action", 1),
        new StatisticItem("Adventure", 1), new StatisticItem("Strategy", 1)), statistics);
  }

  @Test
  void sortsByCountDescending() {
    // given
    StatisticsCalculator calculator = calculatorWith(StatisticAttribute.DEVELOPER,
        game("A", 2000, "RPG"), game("B", 2000, "RPG"), game("B", 2000, "RPG"),
        game("C", 2000, "RPG"), game("C", 2000, "RPG"), game("C", 2000, "RPG"));

    // when
    List<StatisticItem> statistics = calculator.getSortedStatistics();

    // then
    assertEquals(List.of("C", "B", "A"), statistics.stream().map(StatisticItem::value).toList());
  }

  @Test
  void sortsItemsWithEqualCountByValue() {
    // given
    StatisticsCalculator calculator = calculatorWith(StatisticAttribute.DEVELOPER,
        game("Zeta", 2000, "RPG"), game("Alpha", 2000, "RPG"), game("Mid", 2000, "RPG"));

    // when
    List<StatisticItem> statistics = calculator.getSortedStatistics();

    // then
    assertEquals(List.of("Alpha", "Mid", "Zeta"),
        statistics.stream().map(StatisticItem::value).toList());
  }

  @Test
  void returnsEmptyStatisticsWhenNoGamesAdded() {
    // given
    StatisticsCalculator calculator = new StatisticsCalculator(StatisticAttribute.GENRE);

    // when / then
    assertTrue(calculator.getSortedStatistics().isEmpty());
    assertEquals(0, calculator.getGamesProcessed());
  }

  @Test
  void ignoresMissingValuesButStillCountsGame() {
    // given
    StatisticsCalculator calculator = calculatorWith(StatisticAttribute.DEVELOPER,
        new Game("No developer", null, null, List.of()), game("D1", 2000, "RPG"));

    // when
    List<StatisticItem> statistics = calculator.getSortedStatistics();

    // then
    assertEquals(List.of(new StatisticItem("D1", 1)), statistics);
    assertEquals(2, calculator.getGamesProcessed());
  }

  @Test
  void mergeSumsCountsOfBothCalculators() {
    // given
    StatisticsCalculator first = calculatorWith(StatisticAttribute.GENRE,
        game("D1", 2000, "RPG", "Action"));
    StatisticsCalculator second = calculatorWith(StatisticAttribute.GENRE,
        game("D2", 2001, "RPG"), game("D3", 2002, "Puzzle"));

    // when
    first.merge(second);

    // then
    assertEquals(List.of(new StatisticItem("RPG", 2), new StatisticItem("Action", 1),
        new StatisticItem("Puzzle", 1)), first.getSortedStatistics());
    assertEquals(3, first.getGamesProcessed());
  }

  @Test
  void mergeRejectsCalculatorOfAnotherAttribute() {
    // given
    StatisticsCalculator byGenre = new StatisticsCalculator(StatisticAttribute.GENRE);
    StatisticsCalculator byDeveloper = new StatisticsCalculator(StatisticAttribute.DEVELOPER);

    // when / then
    assertThrows(IllegalArgumentException.class, () -> byGenre.merge(byDeveloper));
  }

  private static StatisticsCalculator calculatorWith(StatisticAttribute attribute, Game... games) {
    StatisticsCalculator calculator = new StatisticsCalculator(attribute);
    for (Game game : games) {
      calculator.add(game);
    }
    return calculator;
  }

  private static Game game(String developer, int year, String... genres) {
    return new Game("Title", new Developer(developer), year, List.of(genres));
  }
}
