package com.profitsoft.gamestats.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.profitsoft.gamestats.model.Developer;
import com.profitsoft.gamestats.model.Game;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameJsonParserTest {

  @TempDir
  Path tempDir;

  private final GameJsonParser parser = new GameJsonParser();

  @Test
  void parsesAllFieldsOfSingleGame() throws IOException {
    // given
    String json = """
        [{"title": "The Witcher 3", "developer": "CD Projekt Red",
          "year_released": 2015, "genre": "RPG, Action Adventure"}]
        """;

    // when
    List<Game> games = parse(json);

    // then
    assertEquals(List.of(new Game("The Witcher 3", new Developer("CD Projekt Red"), 2015,
        List.of("RPG", "Action Adventure"))), games);
  }

  @Test
  void parsesMultipleGamesInFileOrder() throws IOException {
    // given
    String json = """
        [{"title": "A", "developer": "D1", "year_released": 2000, "genre": "RPG"},
         {"title": "B", "developer": "D2", "year_released": 2001, "genre": "Action"}]
        """;

    // when
    List<Game> games = parse(json);

    // then
    assertEquals(List.of("A", "B"), games.stream().map(Game::title).toList());
  }

  @Test
  void returnsNoGamesForEmptyArray() throws IOException {
    // when
    List<Game> games = parse("[]");

    // then
    assertTrue(games.isEmpty());
  }

  @Test
  void trimsGenresAndDropsEmptyAndDuplicateOnes() throws IOException {
    // given
    String json = """
        [{"title": "A", "genre": " RPG ,Open World, ,RPG,"}]
        """;

    // when
    List<Game> games = parse(json);

    // then
    assertEquals(List.of("RPG", "Open World"), games.getFirst().genres());
  }

  @Test
  void usesEmptyGenreListWhenGenreIsMissing() throws IOException {
    // when
    List<Game> games = parse("[{\"title\": \"A\"}]");

    // then
    assertTrue(games.getFirst().genres().isEmpty());
  }

  @Test
  void leavesDeveloperEmptyWhenFieldIsMissing() throws IOException {
    // when
    List<Game> games = parse("[{\"title\": \"A\"}]");

    // then
    assertNull(games.getFirst().developer());
  }

  @Test
  void doesNotTreatJsonNullDeveloperAsText() throws IOException {
    // when
    List<Game> games = parse("[{\"title\": \"A\", \"developer\": null}]");

    // then
    assertNull(games.getFirst().developer());
  }

  @Test
  void leavesDeveloperEmptyWhenNameIsBlank() throws IOException {
    // when
    List<Game> games = parse("[{\"title\": \"A\", \"developer\": \"   \"}]");

    // then
    assertNull(games.getFirst().developer());
  }

  @Test
  void acceptsYearWrittenAsString() throws IOException {
    // when
    List<Game> games = parse("[{\"title\": \"A\", \"year_released\": \"2015\"}]");

    // then
    assertEquals(2015, games.getFirst().yearReleased());
  }

  @Test
  void leavesYearEmptyWhenItIsNotANumber() throws IOException {
    // when
    List<Game> games = parse("[{\"title\": \"A\", \"year_released\": \"soon\"}]");

    // then
    assertNull(games.getFirst().yearReleased());
  }

  @Test
  void skipsUnknownFieldsWithNestedStructures() throws IOException {
    // given
    String json = """
        [{"title": "A", "meta": {"developer": "Wrong", "tags": [1, 2, {"x": 1}]},
          "developer": "Right", "year_released": 2000, "genre": "RPG"}]
        """;

    // when
    List<Game> games = parse(json);

    // then
    assertEquals(new Game("A", new Developer("Right"), 2000, List.of("RPG")), games.getFirst());
  }

  @Test
  void throwsWhenTopLevelValueIsNotArray() {
    // given
    String json = "{\"title\": \"A\"}";

    // when / then
    assertThrows(IOException.class, () -> parse(json));
  }

  @Test
  void throwsWhenArrayElementIsNotObject() {
    // when / then
    assertThrows(IOException.class, () -> parse("[1, 2]"));
  }

  @Test
  void throwsOnMalformedJson() {
    // when / then
    assertThrows(IOException.class, () -> parse("[{\"title\": \"A\""));
  }

  @Test
  void throwsOnEmptyFile() {
    // when / then
    assertThrows(IOException.class, () -> parse(""));
  }

  @Test
  void throwsWhenFileDoesNotExist() {
    // given
    Path missing = tempDir.resolve("missing.json");

    // when / then
    assertThrows(IOException.class, () -> parser.parseFile(missing, game -> { }));
  }

  private List<Game> parse(String json) throws IOException {
    Path file = tempDir.resolve("games.json");
    Files.writeString(file, json);
    List<Game> games = new ArrayList<>();
    parser.parseFile(file, games::add);
    return games;
  }
}
