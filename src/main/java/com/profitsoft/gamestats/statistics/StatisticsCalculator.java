package com.profitsoft.gamestats.statistics;

import com.profitsoft.gamestats.model.Game;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StatisticsCalculator {

  private static final Comparator<StatisticItem> BY_COUNT_DESC_THEN_VALUE =
      Comparator.comparingInt(StatisticItem::count).reversed()
          .thenComparing(StatisticItem::value);

  private final StatisticAttribute attribute;
  private final Map<String, Integer> counts = new HashMap<>();
  private int gamesProcessed;

  public StatisticsCalculator(StatisticAttribute attribute) {
    this.attribute = Objects.requireNonNull(attribute, "attribute");
  }

  public void add(Game game) {
    gamesProcessed++;
    for (String value : attribute.valuesOf(game)) {
      counts.merge(value, 1, Integer::sum);
    }
  }

  public void merge(StatisticsCalculator other) {
    if (other.attribute != attribute) {
      throw new IllegalArgumentException(
          "Cannot merge statistics by " + other.attribute + " into statistics by " + attribute);
    }
    other.counts.forEach((value, count) -> counts.merge(value, count, Integer::sum));
    gamesProcessed += other.gamesProcessed;
  }

  public int getGamesProcessed() {
    return gamesProcessed;
  }

  public List<StatisticItem> getSortedStatistics() {
    return counts.entrySet().stream()
        .map(entry -> new StatisticItem(entry.getKey(), entry.getValue()))
        .sorted(BY_COUNT_DESC_THEN_VALUE)
        .toList();
  }
}
