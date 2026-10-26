package com.profitsoft.gamestats.benchmark;

import com.profitsoft.gamestats.statistics.DirectoryStatisticsProcessor;
import com.profitsoft.gamestats.statistics.StatisticAttribute;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.LongStream;

public class ThreadBenchmark {

  private static final int[] THREAD_COUNTS = {1, 2, 4, 8};
  private static final int DEFAULT_RUNS = 5;

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("Usage: ThreadBenchmark <directory> <attribute> [runs]");
      System.exit(1);
    }
    Path directory = Path.of(args[0]);
    StatisticAttribute attribute = StatisticAttribute.fromName(args[1]);
    int runs = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_RUNS;

    System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
    System.out.println("Runs per thread count: " + runs + " (after 1 warm-up run)");
    System.out.println();
    System.out.println("| Threads | Average, ms | Min, ms | Max, ms |");
    System.out.println("|---------|-------------|---------|---------|");
    for (int threadCount : THREAD_COUNTS) {
      measure(directory, attribute, threadCount, runs);
    }
  }

  private static void measure(Path directory, StatisticAttribute attribute, int threadCount,
      int runs) throws IOException {
    DirectoryStatisticsProcessor processor = new DirectoryStatisticsProcessor(threadCount);
    processor.process(directory, attribute);

    long[] timings = new long[runs];
    for (int i = 0; i < runs; i++) {
      long start = System.nanoTime();
      processor.process(directory, attribute);
      timings[i] = (System.nanoTime() - start) / 1_000_000;
    }
    System.out.printf("| %d | %.0f | %d | %d |%n", threadCount,
        LongStream.of(timings).average().orElse(0),
        LongStream.of(timings).min().orElse(0),
        LongStream.of(timings).max().orElse(0));
  }
}
