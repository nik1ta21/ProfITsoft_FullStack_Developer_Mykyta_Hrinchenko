package com.profitsoft.gamestats.model;

import java.util.List;

public record Game(String title, Developer developer, Integer yearReleased, List<String> genres) {

  public Game {
    genres = genres == null ? List.of() : List.copyOf(genres);
  }
}
