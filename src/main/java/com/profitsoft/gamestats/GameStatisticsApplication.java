package com.profitsoft.gamestats;

import com.profitsoft.gamestats.model.Game;
import com.profitsoft.gamestats.parser.GameJsonParser;
import com.profitsoft.gamestats.parser.ParallelGameJsonParser;
import com.profitsoft.gamestats.statistics.StatisticItem;
import com.profitsoft.gamestats.statistics.StatisticsCalculator;
import com.profitsoft.gamestats.xml.XmlGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GameStatisticsApplication {

    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        String directoryPath = args[0];
        String attributeName = args[1];
        int threadCount = 1;

        if (args.length >= 3) {
            try {
                threadCount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid thread count: " + args[2]);
                System.exit(1);
            }
        }

        try {
            long startTime = System.currentTimeMillis();
            List<Game> games = parseGames(directoryPath, threadCount);
            long parseTime = System.currentTimeMillis() - startTime;

            startTime = System.currentTimeMillis();
            StatisticsCalculator calculator = new StatisticsCalculator();
            List<StatisticItem> statistics = calculator.calculateStatistics(games, attributeName);
            long calcTime = System.currentTimeMillis() - startTime;

            String outputFileName = "statistics_by_" + attributeName + ".xml";
            Path outputPath = Paths.get(outputFileName);
            
            startTime = System.currentTimeMillis();
            XmlGenerator xmlGenerator = new XmlGenerator();
            xmlGenerator.generateStatisticsXml(statistics, outputPath);
            long xmlTime = System.currentTimeMillis() - startTime;

            System.out.println("Statistics generated successfully: " + outputFileName);
            System.out.println("Total games processed: " + games.size());
            System.out.println("Unique values: " + statistics.size());
            System.out.println("Parsing time: " + parseTime + "ms");
            System.out.println("Calculation time: " + calcTime + "ms");
            System.out.println("XML generation time: " + xmlTime + "ms");
            System.out.println("Threads used: " + threadCount);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static List<Game> parseGames(String directoryPath, int threadCount) throws IOException {
        Path path = Paths.get(directoryPath);
        
        if (threadCount == 1) {
            GameJsonParser parser = new GameJsonParser();
            return parser.parseDirectory(path);
        } else {
            ParallelGameJsonParser parser = new ParallelGameJsonParser(threadCount);
            return parser.parseDirectory(path);
        }
    }

    private static void printUsage() {
        System.err.println("""
        Usage:
          mvn exec:java "-Dexec.args=<directory> <attribute> [threadCount]"

        Arguments:
          <directory>   Path to the folder containing input JSON files (required)
          <attribute>   Target attribute for statistics: developer | year_released | genre (required)
          [threadCount] Number of worker threads for parallel parsing (optional, default: 1)

        Example:
          mvn exec:java "-Dexec.args=src/main/resources/sample-data genre 4"
        """);
    }
}
