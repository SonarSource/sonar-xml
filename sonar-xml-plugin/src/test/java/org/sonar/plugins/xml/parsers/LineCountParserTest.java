/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.plugins.xml.parsers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.plugins.xml.FileUtils;
import org.sonar.plugins.xml.LineCountData;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThat;

public class LineCountParserTest {

  private static final Charset CHARSET = StandardCharsets.UTF_8;
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testSimpleLineCountParser() throws Exception {
    LineCountData data = newLineCountData("src/test/resources/parsers/linecount/simple.xml");

    assertThat(data.effectiveCommentLines()).containsOnly(3);
    assertThat(data.linesNumber()).isEqualTo(18);
    assertThat(data.linesOfCodeLines()).hasSize(15);
    assertThat(data.linesOfCodeLines()).containsOnly(1, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18);
  }

  @Test
  public void testComplexLineCountParser() throws Exception {
    LineCountData data = newLineCountData("src/test/resources/parsers/linecount/complex.xml");

    assertThat(data.effectiveCommentLines()).hasSize(12);
    assertThat(data.effectiveCommentLines()).containsOnly(4, 5, 8, 9, 10, 21, 23, 24, 28, 29, 30, 33);
    assertThat(data.linesOfCodeLines()).hasSize(21);
    assertThat(data.linesOfCodeLines()).containsOnly(1, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 25, 26, 27, 34, 35, 36, 37, 38, 39);
    assertThat(data.linesNumber()).isEqualTo(40);
  }

  // SONARPLUGINS-1760
  @Test(expected = SAXException.class)
  public void shouldNotInfiniteLoopWhenParsingInvalidXml() throws Exception {
    newLineCountData("src/test/resources/parsers/linecount/invalid.xml");
  }

  @Test
  public void testParseUtf16() throws Exception {
    Charset charset = StandardCharsets.UTF_16;
    LineCountData data = new LineCountParser(newInputFile("src/test/resources/checks/generic/utf16.xml", charset), charset).getLineCountData();
    assertThat(data.linesNumber()).isEqualTo(7);
  }

  private String newInputFile(String path) throws IOException {
    return newInputFile(path, CHARSET);
  }

  private String newInputFile(String path, Charset charset) throws IOException {
    return FileUtils.contents(Paths.get(path), charset);
  }

  private LineCountData newLineCountData(String path) throws Exception {
    return new LineCountParser(newInputFile(path), CHARSET).getLineCountData();
  }
}
