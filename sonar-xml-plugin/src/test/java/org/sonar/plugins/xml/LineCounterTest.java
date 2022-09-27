/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.xml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonarsource.analyzer.commons.xml.ParseException;
import org.sonarsource.analyzer.commons.xml.XmlFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class LineCounterTest {

  private FileLinesContextFactory fileLinesContextFactory;
  private FileLinesContext fileLinesContext;

  @BeforeEach
  void setUp() {
    fileLinesContextFactory = mock(FileLinesContextFactory.class);
    fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
  }

  @Test
  void test_simple_file() throws IOException {
    verifyMetrics("simple.xml", 1,
      1, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18);
  }

  @Test
  void test_complex_file() throws IOException {
    verifyMetrics("complex.xml", 17,
      1, 2, 3, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 25, 26, 27, 34, 35, 36, 37, 38, 39);
  }

  @Test
  void test_invalid_file() {
    assertThrows(ParseException.class, () -> verifyMetrics("invalid.xml", -1));
  }

  @Test // SONARXML-19
  void test_file_with_char_before_prolog() throws Exception {
    verifyMetrics("char_before_prolog.xml", 1, 3, 6, 7, 8, 9, 10, 11, 12, 13);
  }

  private void verifyMetrics(String filename, int commentLinesNumber, int... linesOfCode) throws IOException {
    File moduleBaseDir = new File("src/test/resources/parsers/linecount");
    SensorContextTester context = SensorContextTester.create(moduleBaseDir);
    InputFile inputFile = createInputFile(moduleBaseDir.toPath(), filename);
    LineCounter.analyse(context, fileLinesContextFactory,  XmlFile.create(inputFile));

    Arrays.stream(linesOfCode).forEach(line ->
      verify(fileLinesContext).setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, 1));

    verify(fileLinesContext).save();
    verifyNoMoreInteractions(fileLinesContext);

    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(linesOfCode.length);
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
