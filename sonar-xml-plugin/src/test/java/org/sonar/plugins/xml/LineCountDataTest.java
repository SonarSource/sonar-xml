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
package org.sonar.plugins.xml;

import com.google.common.base.Charsets;
import org.junit.Test;
import org.sonar.plugins.xml.parsers.LineCountParser;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class LineCountDataTest {

  @Test
  public void test_updateAccordingTo_a_delta() throws Exception {
    LineCountData data = new LineCountParser(new File("src/test/resources/parsers/linecount/simple.xml"), Charsets.UTF_8)
      .getLineCountData();

    data.updateAccordingTo(3);

    assertThat(data.effectiveCommentLines()).containsOnly(6);
    assertThat(data.linesNumber()).isEqualTo(21);
    assertThat(data.linesOfCodeLines()).hasSize(15);
    assertThat(data.linesOfCodeLines()).containsOnly(4, 7, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21);
  }

}
