/*
 * Sonar XML Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
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

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.utils.SonarException;

import java.io.File;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

public class LineCountParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testSimpleLineCountParser() throws IOException {
    LineCountParser parser = new LineCountParser();
    int numCommentLine = parser.countLinesOfComment(FileUtils.openInputStream(new File("src/test/resources/parsers/linecount/simple.xml")));

    assertThat(numCommentLine).isEqualTo(1);
  }

  @Test
  @Ignore("SONARPLUGINS-2623")
  public void testComplexLineCountParser() throws IOException {
    LineCountParser parser = new LineCountParser();
    int numCommentLine = parser.countLinesOfComment(FileUtils.openInputStream(new File("src/test/resources/parsers/linecount/complex.xml")));

    assertThat(numCommentLine).isEqualTo(4);
  }

  // SONARPLUGINS-1760
  @Test
  public void shouldNotInfiniteLoopWhenParsingInvalidXml() throws IOException {
    LineCountParser parser = new LineCountParser();

    thrown.expect(SonarException.class);

    parser.countLinesOfComment(FileUtils.openInputStream(new File("src/test/resources/parsers/linecount/invalid.xml")));
  }

}
