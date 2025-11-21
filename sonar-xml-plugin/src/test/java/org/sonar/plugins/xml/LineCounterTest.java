/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonarsource.analyzer.commons.xml.ParseException;
import org.sonarsource.analyzer.commons.xml.XmlFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineCounterTest {

  @Test
  void test_simple_file() throws IOException {
    verifyMetrics("simple.xml", 1, """
        {"ncloc_data":{"1":1,"4":1,"5":1,"6":1,"7":1,"8":1,"9":1,"10":1,"12":1,"13":1,"14":1,"15":1,"16":1,"17":1,"18":1}}""");
  }

  @Test
  void test_complex_file() throws IOException {
    verifyMetrics("complex.xml", 17, """
      {"ncloc_data":{"1":1,"2":1,"3":1,"11":1,"12":1,"13":1,"14":1,"15":1,"16":1,"17":1,"18":1,"19":1,"20":1,"22":1,"25":1,"26":1,"27":1,"34":1,"35":1,"36":1,"37":1,"38":1,"39":1}}""");
  }

  @Test
  void test_invalid_file() {
    assertThrows(ParseException.class, () -> verifyMetrics("invalid.xml", -1, ""));
  }

  @Test // SONARXML-19
  void test_file_with_char_before_prolog() throws Exception {
    verifyMetrics("char_before_prolog.xml", 1, """
      {"ncloc_data":{"3":1,"6":1,"7":1,"8":1,"9":1,"10":1,"11":1,"12":1,"13":1}}""");
  }

  private void verifyMetrics(String filename, int commentLinesNumber, String linesMetrics) throws IOException {
    File moduleBaseDir = new File("src/test/resources/parsers/linecount");
    SensorContextTester context = SensorContextTester.create(moduleBaseDir);
    InputFile inputFile = createInputFile(moduleBaseDir.toPath(), filename);
    var fileLinesContext = new FileLinesContextTester();
    LineCounter.analyse(context, fileLinesContext, XmlFile.create(inputFile));
    Map<String, Map<Integer, Object>> metrics = fileLinesContext.metrics(inputFile);
    assertThat(new Gson().toJson(metrics)).isEqualTo(linesMetrics);
    int expectedNcloc = metrics.get(CoreMetrics.NCLOC_DATA_KEY).size();
    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(expectedNcloc);
    assertThat(context.measure(inputFile.key(), CoreMetrics.COMMENT_LINES).value()).isEqualTo(commentLinesNumber);
  }

  private InputFile createInputFile(Path moduleBaseDir, String name) {
    return TestInputFileBuilder.create("modulekey", name)
      .setModuleBaseDir(moduleBaseDir)
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(StandardCharsets.UTF_8)
      .build();
  }
}
