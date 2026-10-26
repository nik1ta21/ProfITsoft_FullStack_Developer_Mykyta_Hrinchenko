package com.profitsoft.gamestats.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.profitsoft.gamestats.model.Developer;
import com.profitsoft.gamestats.model.Game;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Streaming parser of a JSON file with an array of games. Games are passed to the consumer
 * one at a time, so memory usage does not depend on the file size. Stateless and thread-safe.
 */
public class GameJsonParser {

  private static final JsonFactory JSON_FACTORY = new JsonFactory();

  public void parseFile(Path file, Consumer<Game> consumer) throws IOException {
    try (InputStream input = Files.newInputStream(file);
        JsonParser parser = JSON_FACTORY.createParser(input)) {
      if (parser.nextToken() != JsonToken.START_ARRAY) {
        throw new IOException("Expected a JSON array at the top level");
      }
      while (parser.nextToken() != JsonToken.END_ARRAY) {
        consumer.accept(parseGame(parser));
      }
    }
  }

  private Game parseGame(JsonParser parser) throws IOException {
    if (parser.currentToken() != JsonToken.START_OBJECT) {
      throw new IOException("Expected a JSON object in the array, found: " + parser.currentToken());
    }
    String title = null;
    Developer developer = null;
    Integer yearReleased = null;
    List<String> genres = List.of();

    while (parser.nextToken() != JsonToken.END_OBJECT) {
      String fieldName = parser.currentName();
      parser.nextToken();
      switch (fieldName) {
        case "title" -> title = readText(parser);
        case "developer" -> developer = toDeveloper(readText(parser));
        case "year_released" -> yearReleased = readYear(parser);
        case "genre" -> genres = splitGenres(readText(parser));
        default -> parser.skipChildren();
      }
    }
    return new Game(title, developer, yearReleased, genres);
  }

  /** Returns the string value, or null for any other kind of value (null, number, object...). */
  private String readText(JsonParser parser) throws IOException {
    if (parser.currentToken() == JsonToken.VALUE_STRING) {
      return parser.getText();
    }
    parser.skipChildren();
    return null;
  }

  private Integer readYear(JsonParser parser) throws IOException {
    if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
      return parser.getIntValue();
    }
    return parseYear(readText(parser));
  }

  private Integer parseYear(String text) {
    if (text == null) {
      return null;
    }
    try {
      return Integer.valueOf(text.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Developer toDeveloper(String name) {
    return name == null || name.isBlank() ? null : new Developer(name.trim());
  }

  private List<String> splitGenres(String genreText) {
    if (genreText == null) {
      return List.of();
    }
    return Arrays.stream(genreText.split(","))
        .map(String::trim)
        .filter(genre -> !genre.isEmpty())
        .distinct()
        .toList();
  }
}
