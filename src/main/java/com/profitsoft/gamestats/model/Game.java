package com.profitsoft.gamestats.model;

import java.util.List;

/**
 * Main entity: a video game. Each game has one {@link Developer} (many-to-one).
 * {@code developer} and {@code yearReleased} may be null when absent in the input.
 */
public record Game(String title, Developer developer, Integer yearReleased, List<String> genres) {

  public Game {
    genres = genres == null ? List.of() : List.copyOf(genres);
  }
}
