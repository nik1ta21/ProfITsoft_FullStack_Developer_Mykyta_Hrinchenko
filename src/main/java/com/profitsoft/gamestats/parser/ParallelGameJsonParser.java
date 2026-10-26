package com.profitsoft.gamestats.parser;

import com.profitsoft.gamestats.model.Game;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class ParallelGameJsonParser {
    
    private final GameJsonParser parser;
    private final int threadCount;

    public ParallelGameJsonParser(int threadCount) {
        this.parser = new GameJsonParser();
        this.threadCount = threadCount;
    }

    public List<Game> parseDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("Path is not a directory: " + directory);
        }
        
        List<File> jsonFiles;
        try (Stream<Path> paths = Files.walk(directory)) {
            jsonFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(Path::toFile)
                    .toList();
        }
        
        if (jsonFiles.isEmpty()) {
            return List.of();
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<List<Game>>> futures = new ArrayList<>();
        
        for (File jsonFile : jsonFiles) {
            futures.add(executor.submit(new FileParsingTask(parser, jsonFile)));
        }
        
        List<Game> allGames = new ArrayList<>();
        for (Future<List<Game>> future : futures) {
            try {
                allGames.addAll(future.get());
            } catch (Exception e) {
                throw new IOException("Error parsing file", e);
            }
        }
        
        executor.shutdown();
        return allGames;
    }

    private static class FileParsingTask implements Callable<List<Game>> {
        private final GameJsonParser parser;
        private final File file;

        FileParsingTask(GameJsonParser parser, File file) {
            this.parser = parser;
            this.file = file;
        }

        @Override
        public List<Game> call() throws IOException {
            return parser.parseFile(file);
        }
    }
}
