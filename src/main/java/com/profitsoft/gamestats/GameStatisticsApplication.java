package com.profitsoft.gamestats;

import com.profitsoft.gamestats.statistics.DirectoryStatisticsProcessor;
import com.profitsoft.gamestats.statistics.StatisticAttribute;
import com.profitsoft.gamestats.statistics.StatisticItem;
import com.profitsoft.gamestats.statistics.StatisticsCalculator;
import com.profitsoft.gamestats.xml.XmlGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class GameStatisticsApplication {

  private static final String USAGE = """
      Usage:
        mvn exec:java "-Dexec.args=<directory> <attribute> [threadCount]"

      Arguments:
        <directory>   Path to the folder containing input JSON files
        <attribute>   developer | year_released | genre
        [threadCount] Number of threads for parsing files (optional, default: 1)

      Example:
        mvn exec:java "-Dexec.args=src/main/resources/sample-data genre 4"
      """;

  public static void main(String[] args) {
    try {
      run(args);
    } catch (IllegalArgumentException e) {
      System.err.println("Error: " + e.getMessage());
      System.err.println(USAGE);
      System.exit(1);
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  private static void run(String[] args) throws IOException {
    if (args.length < 2 || args.length > 3) {
      throw new IllegalArgumentException("Expected 2 or 3 arguments, got: " + args.length);
    }
    Path directory = Path.of(args[0]);
    StatisticAttribute attribute = StatisticAttribute.fromName(args[1]);
    int threadCount = args.length == 3 ? parseThreadCount(args[2]) : 1;
    DirectoryStatisticsProcessor processor = new DirectoryStatisticsProcessor(threadCount);

    long start = System.nanoTime();
    StatisticsCalculator calculator = processor.process(directory, attribute);
    long processingMillis = millisSince(start);

    List<StatisticItem> statistics = calculator.getSortedStatistics();
    Path outputPath = Path.of("statistics_by_" + attribute.getName() + ".xml");
    start = System.nanoTime();
    new XmlGenerator().generateStatisticsXml(statistics, outputPath);
    long xmlMillis = millisSince(start);

    System.out.println("Statistics generated successfully: " + outputPath);
    System.out.println("Total games processed: " + calculator.getGamesProcessed());
    System.out.println("Unique values: " + statistics.size());
    System.out.println("Threads used: " + threadCount);
    System.out.println("Parsing and counting time: " + processingMillis + " ms");
    System.out.println("XML generation time: " + xmlMillis + " ms");
  }

  private static int parseThreadCount(String text) {
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid thread count: " + text);
    }
  }

  private static long millisSince(long startNanos) {
    return (System.nanoTime() - startNanos) / 1_000_000;
  }
}
