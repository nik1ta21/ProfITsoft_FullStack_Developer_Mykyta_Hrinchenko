package com.profitsoft.gamestats.xml;

import com.profitsoft.gamestats.statistics.StatisticItem;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class XmlGenerator {

  public void generateStatisticsXml(List<StatisticItem> items, Path outputPath)
      throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      writer.write("<statistics>\n");
      for (StatisticItem item : items) {
        writeItem(writer, item);
      }
      writer.write("</statistics>\n");
    }
  }

  private void writeItem(BufferedWriter writer, StatisticItem item) throws IOException {
    writer.write("  <item>\n");
    writer.write("    <value>" + escapeXml(item.value()) + "</value>\n");
    writer.write("    <count>" + item.count() + "</count>\n");
    writer.write("  </item>\n");
  }

  private String escapeXml(String input) {
    return input.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }
}
