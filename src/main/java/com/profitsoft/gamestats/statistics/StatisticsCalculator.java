package com.profitsoft.gamestats.statistics;

import com.profitsoft.gamestats.model.Game;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsCalculator {

    public List<StatisticItem> calculateStatistics(List<Game> games, String attributeName) {
        Map<String, Integer> counts = new HashMap<>();
        
        for (Game game : games) {
            List<String> values = getAttributeValues(game, attributeName);
            for (String value : values) {
                counts.merge(value, 1, Integer::sum);
            }
        }
        
        List<StatisticItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            items.add(new StatisticItem(entry.getKey(), entry.getValue()));
        }
        
        items.sort(Comparator.comparingInt(StatisticItem::getCount).reversed());
        return items;
    }

    private List<String> getAttributeValues(Game game, String attributeName) {
        return switch (attributeName) {
            case "developer" -> List.of(game.getDeveloper());
            case "year_released" -> List.of(String.valueOf(game.getYearReleased()));
            case "genre" -> game.getGenres() != null ? game.getGenres() : List.of();
            default -> throw new IllegalArgumentException("Unknown attribute: " + attributeName);
        };
    }
}
