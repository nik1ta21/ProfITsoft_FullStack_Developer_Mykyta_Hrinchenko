package com.profitsoft.gamestats.xml;

import com.profitsoft.gamestats.statistics.StatisticItem;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class XmlGenerator {

    public void generateStatisticsXml(List<StatisticItem> items, Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<statistics>\n");
            
            for (StatisticItem item : items) {
                writer.write("  <item>\n");
                writer.write("    <value>" + escapeXml(item.getValue()) + "</value>\n");
                writer.write("    <count>" + item.getCount() + "</count>\n");
                writer.write("  </item>\n");
            }
            
            writer.write("</statistics>\n");
        }
    }

    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&apos;");
    }
}
