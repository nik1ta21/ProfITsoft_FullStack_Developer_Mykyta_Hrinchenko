package com.profitsoft.gamestats.statistics;

import com.profitsoft.gamestats.model.Game;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum StatisticAttribute {
  DEVELOPER("developer",
      game -> game.developer() == null ? List.of() : List.of(game.developer().name())),
  YEAR_RELEASED("year_released",
      game -> game.yearReleased() == null ? List.of() : List.of(String.valueOf(game.yearReleased()))),
  GENRE("genre", Game::genres);

  private final String name;
  private final Function<Game, List<String>> valueExtractor;

  StatisticAttribute(String name, Function<Game, List<String>> valueExtractor) {
    this.name = name;
    this.valueExtractor = valueExtractor;
  }

  public String getName() {
    return name;
  }

  public List<String> valuesOf(Game game) {
    return valueExtractor.apply(game);
  }

  public static StatisticAttribute fromName(String name) {
    return Arrays.stream(values())
        .filter(attribute -> attribute.name.equals(name))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Unknown attribute: " + name + ". Supported: " + supportedNames()));
  }

  private static String supportedNames() {
    return Arrays.stream(values())
        .map(StatisticAttribute::getName)
        .collect(Collectors.joining(", "));
  }
}
