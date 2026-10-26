package com.profitsoft.gamestats.statistics;

import com.profitsoft.gamestats.model.Game;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class StatisticsCalculatorTest {

    @Test
    void testCalculateDeveloperStatistics() {
        List<Game> games = List.of(
            new Game("Game1", "Developer1", 2000, List.of("Genre1")),
            new Game("Game2", "Developer1", 2001, List.of("Genre2")),
            new Game("Game3", "Developer2", 2002, List.of("Genre1"))
        );
        
        StatisticsCalculator calculator = new StatisticsCalculator();
        List<StatisticItem> stats = calculator.calculateStatistics(games, "developer");
        
        assertEquals(2, stats.size());
        assertEquals("Developer1", stats.get(0).getValue());
        assertEquals(2, stats.get(0).getCount());
        assertEquals("Developer2", stats.get(1).getValue());
        assertEquals(1, stats.get(1).getCount());
    }

    @Test
    void testCalculateYearStatistics() {
        List<Game> games = List.of(
            new Game("Game1", "Developer1", 2000, List.of("Genre1")),
            new Game("Game2", "Developer2", 2000, List.of("Genre2")),
            new Game("Game3", "Developer3", 2001, List.of("Genre1"))
        );
        
        StatisticsCalculator calculator = new StatisticsCalculator();
        List<StatisticItem> stats = calculator.calculateStatistics(games, "year_released");
        
        assertEquals(2, stats.size());
        assertEquals("2000", stats.get(0).getValue());
        assertEquals(2, stats.get(0).getCount());
        assertEquals("2001", stats.get(1).getValue());
        assertEquals(1, stats.get(1).getCount());
    }

    @Test
    void testCalculateGenreStatistics() {
        List<Game> games = List.of(
            new Game("Game1", "Developer1", 2000, List.of("RPG", "Action")),
            new Game("Game2", "Developer2", 2001, List.of("RPG", "Adventure")),
            new Game("Game3", "Developer3", 2002, List.of("Strategy"))
        );
        
        StatisticsCalculator calculator = new StatisticsCalculator();
        List<StatisticItem> stats = calculator.calculateStatistics(games, "genre");
        
        assertEquals(4, stats.size());
        assertEquals("RPG", stats.get(0).getValue());
        assertEquals(2, stats.get(0).getCount());
    }

    @Test
    void testCalculateStatisticsWithEmptyList() {
        List<Game> games = List.of();
        
        StatisticsCalculator calculator = new StatisticsCalculator();
        List<StatisticItem> stats = calculator.calculateStatistics(games, "developer");
        
        assertTrue(stats.isEmpty());
    }

    @Test
    void testCalculateStatisticsWithInvalidAttribute() {
        List<Game> games = List.of(
            new Game("Game1", "Developer1", 2000, List.of("Genre1"))
        );
        
        StatisticsCalculator calculator = new StatisticsCalculator();
        
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateStatistics(games, "invalid");
        });
    }

    @Test
    void testStatisticsSortedByCount() {
        List<Game> games = List.of(
            new Game("Game1", "Developer1", 2000, List.of("Genre1")),
            new Game("Game2", "Developer2", 2000, List.of("Genre2")),
            new Game("Game3", "Developer3", 2000, List.of("Genre3")),
            new Game("Game4", "Developer1", 2000, List.of("Genre1"))
        );
        
        StatisticsCalculator calculator = new StatisticsCalculator();
        List<StatisticItem> stats = calculator.calculateStatistics(games, "developer");
        
        assertEquals(3, stats.size());
        assertTrue(stats.get(0).getCount() >= stats.get(1).getCount());
        assertTrue(stats.get(1).getCount() >= stats.get(2).getCount());
    }
}
