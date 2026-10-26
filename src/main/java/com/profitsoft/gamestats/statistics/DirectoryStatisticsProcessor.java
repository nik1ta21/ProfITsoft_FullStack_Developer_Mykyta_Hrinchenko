package com.profitsoft.gamestats.statistics;

import com.profitsoft.gamestats.parser.GameJsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * Calculates statistics for all JSON files of a directory. Every file is parsed in a separate
 * task of a fixed thread pool; each task has its own calculator, results are merged at the end.
 */
public class DirectoryStatisticsProcessor {

  private final GameJsonParser parser = new GameJsonParser();
  private final int threadCount;

  public DirectoryStatisticsProcessor(int threadCount) {
    if (threadCount < 1) {
      throw new IllegalArgumentException("Thread count must be at least 1, got: " + threadCount);
    }
    this.threadCount = threadCount;
  }

  public StatisticsCalculator process(Path directory, StatisticAttribute attribute)
      throws IOException {
    List<Path> files = findJsonFiles(directory);
    StatisticsCalculator total = new StatisticsCalculator(attribute);

    try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
      List<Future<StatisticsCalculator>> futures = new ArrayList<>();
      for (Path file : files) {
        futures.add(executor.submit(() -> processFile(file, attribute)));
      }
      mergeResults(futures, total);
    }
    return total;
  }

  private List<Path> findJsonFiles(Path directory) throws IOException {
    if (!Files.isDirectory(directory)) {
      throw new IOException("Not a directory: " + directory);
    }
    try (Stream<Path> paths = Files.list(directory)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(DirectoryStatisticsProcessor::isJsonFile)
          .sorted()
          .toList();
    }
  }

  private static boolean isJsonFile(Path path) {
    return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
  }

  private StatisticsCalculator processFile(Path file, StatisticAttribute attribute)
      throws IOException {
    StatisticsCalculator calculator = new StatisticsCalculator(attribute);
    try {
      parser.parseFile(file, calculator::add);
    } catch (IOException e) {
      throw new IOException("Failed to parse " + file + ": " + e.getMessage(), e);
    }
    return calculator;
  }

  private void mergeResults(List<Future<StatisticsCalculator>> futures, StatisticsCalculator total)
      throws IOException {
    try {
      for (Future<StatisticsCalculator> future : futures) {
        total.merge(future.get());
      }
    } catch (ExecutionException e) {
      cancelAll(futures);
      throw asIoException(e.getCause());
    } catch (InterruptedException e) {
      cancelAll(futures);
      Thread.currentThread().interrupt();
      throw new IOException("Processing was interrupted", e);
    }
  }

  private void cancelAll(List<Future<StatisticsCalculator>> futures) {
    futures.forEach(future -> future.cancel(true));
  }

  private IOException asIoException(Throwable cause) {
    return cause instanceof IOException ioException
        ? ioException
        : new IOException("Unexpected error while processing files", cause);
  }
}
