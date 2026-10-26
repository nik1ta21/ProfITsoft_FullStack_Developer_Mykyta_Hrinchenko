package com.profitsoft.gamestats.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.profitsoft.gamestats.statistics.StatisticItem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class XmlGeneratorTest {

  @TempDir
  Path tempDir;

  private final XmlGenerator generator = new XmlGenerator();

  @Test
  void writesItemsInGivenOrder() throws IOException {
    // given
    List<StatisticItem> items = List.of(new StatisticItem("RPG", 2), new StatisticItem("Action", 1));

    // when
    String xml = generate(items);

    // then
    assertEquals("""
        <?xml version="1.0" encoding="UTF-8"?>
        <statistics>
          <item>
            <value>RPG</value>
            <count>2</count>
          </item>
          <item>
            <value>Action</value>
            <count>1</count>
          </item>
        </statistics>
        """, xml);
  }

  @Test
  void writesEmptyRootForEmptyStatistics() throws IOException {
    // when
    String xml = generate(List.of());

    // then
    assertEquals("""
        <?xml version="1.0" encoding="UTF-8"?>
        <statistics>
        </statistics>
        """, xml);
  }

  @Test
  void escapesSpecialCharacters() throws IOException {
    // given
    List<StatisticItem> items = List.of(new StatisticItem("Tom & <Jerry> \"Co\" 'x'", 1));

    // when
    String xml = generate(items);

    // then
    assertTrue(xml.contains("<value>Tom &amp; &lt;Jerry&gt; &quot;Co&quot; &apos;x&apos;</value>"));
  }

  @Test
  void writesNonAsciiTextAsUtf8() throws IOException {
    // given
    List<StatisticItem> items = List.of(new StatisticItem("Відеоігри", 1));

    // when
    String xml = generate(items);

    // then
    assertTrue(xml.contains("<value>Відеоігри</value>"));
  }

  private String generate(List<StatisticItem> items) throws IOException {
    Path output = tempDir.resolve("statistics.xml");
    generator.generateStatisticsXml(items, output);
    return Files.readString(output, StandardCharsets.UTF_8);
  }
}
