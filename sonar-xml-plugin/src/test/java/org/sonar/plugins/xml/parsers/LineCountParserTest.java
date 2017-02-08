/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import com.google.common.base.Charsets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.plugins.xml.LineCountData;
import org.xml.sax.SAXException;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class LineCountParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testSimpleLineCountParser() throws Exception {
    LineCountData data = new LineCountParser(new File("src/test/resources/parsers/linecount/simple.xml"), Charsets.UTF_8)
      .getLineCountData();

    assertThat(data.effectiveCommentLines()).containsOnly(3);
    assertThat(data.linesNumber()).isEqualTo(18);
    assertThat(data.linesOfCodeLines()).hasSize(15);
    assertThat(data.linesOfCodeLines()).containsOnly(1, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18);
  }

  @Test
  public void testComplexLineCountParser() throws Exception {
    LineCountData data = new LineCountParser(new File("src/test/resources/parsers/linecount/complex.xml"), Charsets.UTF_8)
      .getLineCountData();

    assertThat(data.effectiveCommentLines()).hasSize(12);
    assertThat(data.effectiveCommentLines()).containsOnly(4, 5, 8, 9, 10, 21, 23, 24, 28, 29, 30, 33);
    assertThat(data.linesOfCodeLines()).hasSize(21);
    assertThat(data.linesOfCodeLines()).containsOnly(1, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 25, 26, 27, 34, 35, 36, 37, 38, 39);
    assertThat(data.linesNumber()).isEqualTo(40);
  }

  // SONARPLUGINS-1760
  @Test(expected = SAXException.class)
  public void shouldNotInfiniteLoopWhenParsingInvalidXml() throws Exception {
    new LineCountParser(new File("src/test/resources/parsers/linecount/invalid.xml"), Charsets.UTF_8);
  }

}
