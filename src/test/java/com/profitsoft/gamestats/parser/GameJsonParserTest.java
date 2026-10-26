package com.profitsoft.gamestats.parser;

import com.profitsoft.gamestats.model.Game;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameJsonParserTest {

    @TempDir
    Path tempDir;

    @Test
    void testParseSingleGame() throws IOException {
        String json = """
            [
              {
                "title": "The Witcher 3",
                "developer": "CD Projekt Red",
                "year_released": 2015,
                "genre": "RPG, Action Adventure"
              }
            ]
            """;
        
        Path jsonFile = tempDir.resolve("games.json");
        Files.writeString(jsonFile, json);
        
        GameJsonParser parser = new GameJsonParser();
        List<Game> games = parser.parseFile(jsonFile.toFile());
        
        assertEquals(1, games.size());
        Game game = games.get(0);
        assertEquals("The Witcher 3", game.getTitle());
        assertEquals("CD Projekt Red", game.getDeveloper());
        assertEquals(2015, game.getYearReleased());
        assertEquals(2, game.getGenres().size());
        assertTrue(game.getGenres().contains("RPG"));
        assertTrue(game.getGenres().contains("Action Adventure"));
    }

    @Test
    void testParseMultipleGames() throws IOException {
        String json = """
            [
              {
                "title": "The Witcher 3",
                "developer": "CD Projekt Red",
                "year_released": 2015,
                "genre": "RPG"
              },
              {
                "title": "Cyberpunk 2077",
                "developer": "CD Projekt Red",
                "year_released": 2020,
                "genre": "RPG, Action"
              }
            ]
            """;
        
        Path jsonFile = tempDir.resolve("games.json");
        Files.writeString(jsonFile, json);
        
        GameJsonParser parser = new GameJsonParser();
        List<Game> games = parser.parseFile(jsonFile.toFile());
        
        assertEquals(2, games.size());
    }

    @Test
    void testParseDirectory() throws IOException {
        String json1 = """
            [
              {
                "title": "Game1",
                "developer": "Developer1",
                "year_released": 2000,
                "genre": "Genre1"
              }
            ]
            """;
        
        String json2 = """
            [
              {
                "title": "Game2",
                "developer": "Developer2",
                "year_released": 2001,
                "genre": "Genre2"
              }
            ]
            """;
        
        Files.writeString(tempDir.resolve("file1.json"), json1);
        Files.writeString(tempDir.resolve("file2.json"), json2);
        
        GameJsonParser parser = new GameJsonParser();
        List<Game> games = parser.parseDirectory(tempDir);
        
        assertEquals(2, games.size());
    }

    @Test
    void testParseEmptyArray() throws IOException {
        String json = "[]";
        
        Path jsonFile = tempDir.resolve("empty.json");
        Files.writeString(jsonFile, json);
        
        GameJsonParser parser = new GameJsonParser();
        List<Game> games = parser.parseFile(jsonFile.toFile());
        
        assertTrue(games.isEmpty());
    }

    @Test
    void testParseGenreWithSpaces() throws IOException {
        String json = """
            [
              {
                "title": "Test",
                "developer": "Test Developer",
                "year_released": 2000,
                "genre": "Open World, Action Adventure"
              }
            ]
            """;
        
        Path jsonFile = tempDir.resolve("games.json");
        Files.writeString(jsonFile, json);
        
        GameJsonParser parser = new GameJsonParser();
        List<Game> games = parser.parseFile(jsonFile.toFile());
        
        assertEquals(1, games.size());
        assertEquals(2, games.get(0).getGenres().size());
        assertTrue(games.get(0).getGenres().contains("Open World"));
        assertTrue(games.get(0).getGenres().contains("Action Adventure"));
    }
}
