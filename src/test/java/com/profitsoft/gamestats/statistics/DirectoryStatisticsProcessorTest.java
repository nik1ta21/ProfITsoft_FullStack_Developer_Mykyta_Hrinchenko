package com.profitsoft.gamestats.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DirectoryStatisticsProcessorTest {

  @TempDir
  Path tempDir;

  @Test
  void aggregatesStatisticsOfAllFiles() throws IOException {
    // given
    writeFile("a.json", game("D1", 2000, "RPG, Action"));
    writeFile("b.json", game("D1", 2001, "RPG"), game("D2", 2002, "Puzzle"));

    // when
    StatisticsCalculator result = process(2, StatisticAttribute.GENRE);

    // then
    assertEquals(List.of(new StatisticItem("RPG", 2), new StatisticItem("Action", 1),
        new StatisticItem("Puzzle", 1)), result.getSortedStatistics());
    assertEquals(3, result.getGamesProcessed());
  }

  @Test
  void givesSameResultForAnyNumberOfThreads() throws IOException {
    // given
    for (int i = 0; i < 10; i++) {
      writeFile("games" + i + ".json", game("D" + i % 3, 2000 + i, "RPG, G" + i % 4),
          game("D" + i % 2, 2010, "Action"));
    }

    // when
    List<StatisticItem> single = process(1, StatisticAttribute.GENRE).getSortedStatistics();
    List<StatisticItem> parallel = process(4, StatisticAttribute.GENRE).getSortedStatistics();
    List<StatisticItem> manyThreads = process(8, StatisticAttribute.GENRE).getSortedStatistics();

    // then
    assertEquals(single, parallel);
    assertEquals(single, manyThreads);
  }

  @Test
  void ignoresFilesThatAreNotJson() throws IOException {
    // given
    writeFile("games.json", game("D1", 2000, "RPG"));
    Files.writeString(tempDir.resolve("notes.txt"), "not json at all");

    // when
    StatisticsCalculator result = process(2, StatisticAttribute.DEVELOPER);

    // then
    assertEquals(1, result.getGamesProcessed());
  }

  @Test
  void returnsEmptyStatisticsForDirectoryWithoutJsonFiles() throws IOException {
    // when
    StatisticsCalculator result = process(2, StatisticAttribute.DEVELOPER);

    // then
    assertTrue(result.getSortedStatistics().isEmpty());
    assertEquals(0, result.getGamesProcessed());
  }

  @Test
  void throwsWhenPathIsNotDirectory() throws IOException {
    // given
    Path file = Files.writeString(tempDir.resolve("file.json"), "[]");

    // when / then
    assertThrows(IOException.class,
        () -> new DirectoryStatisticsProcessor(1).process(file, StatisticAttribute.GENRE));
  }

  @Test
  void throwsWhenDirectoryDoesNotExist() {
    // given
    Path missing = tempDir.resolve("missing");

    // when / then
    assertThrows(IOException.class,
        () -> new DirectoryStatisticsProcessor(1).process(missing, StatisticAttribute.GENRE));
  }

  @Test
  void reportsNameOfBrokenFile() throws IOException {
    // given
    writeFile("good.json", game("D1", 2000, "RPG"));
    Files.writeString(tempDir.resolve("broken.json"), "[{\"title\": ");

    // when
    IOException exception = assertThrows(IOException.class,
        () -> process(2, StatisticAttribute.GENRE));

    // then
    assertTrue(exception.getMessage().contains("broken.json"));
  }

  @Test
  void rejectsThreadCountBelowOne() {
    // when / then
    assertThrows(IllegalArgumentException.class, () -> new DirectoryStatisticsProcessor(0));
    assertThrows(IllegalArgumentException.class, () -> new DirectoryStatisticsProcessor(-3));
  }

  private StatisticsCalculator process(int threads, StatisticAttribute attribute)
      throws IOException {
    return new DirectoryStatisticsProcessor(threads).process(tempDir, attribute);
  }

  private void writeFile(String name, String... games) throws IOException {
    Files.writeString(tempDir.resolve(name), "[" + String.join(",", games) + "]");
  }

  private static String game(String developer, int year, String genres) {
    return """
        {"title": "T", "developer": "%s", "year_released": %d, "genre": "%s"}"""
        .formatted(developer, year, genres);
  }
}
