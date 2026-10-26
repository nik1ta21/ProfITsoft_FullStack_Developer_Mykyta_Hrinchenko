package com.profitsoft.gamestats.benchmark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

/**
 * Generates large JSON files with random games for the thread-count experiment.
 * Usage: DataGenerator &lt;outputDir&gt; &lt;fileCount&gt; &lt;gamesPerFile&gt;
 */
public class DataGenerator {

  private static final List<String> GENRES = List.of(
      "RPG", "Action", "Adventure", "Open World", "Shooter", "Strategy", "Simulation", "Sandbox",
      "Survival", "Horror", "Platformer", "Puzzle", "Roguelike", "Sci-Fi", "Fantasy", "Stealth",
      "Racing", "Sports", "Fighting", "MOBA", "Indie", "Metroidvania", "Card Game", "Tactical");
  private static final int DEVELOPER_COUNT = 300;
  private static final int MAX_GENRES_PER_GAME = 4;

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.err.println("Usage: DataGenerator <outputDir> <fileCount> <gamesPerFile>");
      System.exit(1);
    }
    Path outputDir = Path.of(args[0]);
    int fileCount = Integer.parseInt(args[1]);
    int gamesPerFile = Integer.parseInt(args[2]);

    Files.createDirectories(outputDir);
    Random random = new Random(42);
    for (int i = 1; i <= fileCount; i++) {
      Path file = outputDir.resolve("games_" + i + ".json");
      writeFile(file, i, gamesPerFile, random);
      System.out.println("Generated " + file + " (" + Files.size(file) / (1024 * 1024) + " MB)");
    }
  }

  private static void writeFile(Path file, int fileNumber, int gameCount, Random random)
      throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
      writer.write("[\n");
      for (int i = 0; i < gameCount; i++) {
        writer.write(buildGame(fileNumber + "-" + i, random));
        writer.write(i < gameCount - 1 ? ",\n" : "\n");
      }
      writer.write("]\n");
    }
  }

  private static String buildGame(String id, Random random) {
    return """
          {
            "title": "Game %s",
            "developer": "Developer %d",
            "year_released": %d,
            "genre": "%s"
          }""".formatted(id, random.nextInt(DEVELOPER_COUNT), 1990 + random.nextInt(35),
        randomGenres(random));
  }

  private static String randomGenres(Random random) {
    int count = 1 + random.nextInt(MAX_GENRES_PER_GAME);
    return String.join(", ", random.ints(count, 0, GENRES.size())
        .distinct()
        .mapToObj(GENRES::get)
        .toList());
  }
}
