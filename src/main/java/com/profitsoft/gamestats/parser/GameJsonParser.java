package com.profitsoft.gamestats.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.profitsoft.gamestats.model.Game;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GameJsonParser {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    public List<Game> parseFile(File file) throws IOException {
        List<Game> games = new ArrayList<>();
        try (JsonParser parser = JSON_FACTORY.createParser(file)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Expected content to be an array");
            }
            
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                Game game = parseGame(parser);
                if (game != null) {
                    games.add(game);
                }
            }
        }
        return games;
    }

    private Game parseGame(JsonParser parser) throws IOException {
        Game game = new Game();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken();
            
            switch (fieldName) {
                case "title" -> game.setTitle(parser.getText());
                case "developer" -> game.setDeveloper(parser.getText());
                case "year_released" -> game.setYearReleased(parser.getIntValue());
                case "genre" -> game.setGenres(parseGenres(parser.getText()));
                default -> {}
            }
        }
        return game;
    }

    private List<String> parseGenres(String genreString) {
        List<String> genres = new ArrayList<>();
        if (genreString != null && !genreString.isBlank()) {
            String[] parts = genreString.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    genres.add(trimmed);
                }
            }
        }
        return genres;
    }

    public List<Game> parseDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("Path is not a directory: " + directory);
        }
        
        List<Game> allGames = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(directory)) {
            List<File> jsonFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .map(Path::toFile)
                    .toList();
            
            for (File jsonFile : jsonFiles) {
                allGames.addAll(parseFile(jsonFile));
            }
        }
        return allGames;
    }
}
