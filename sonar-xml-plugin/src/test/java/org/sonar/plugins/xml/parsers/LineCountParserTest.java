/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.xml.parsers;

import com.google.common.base.Charsets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.plugins.xml.LineCountData;
import org.xml.sax.SAXException;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

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
