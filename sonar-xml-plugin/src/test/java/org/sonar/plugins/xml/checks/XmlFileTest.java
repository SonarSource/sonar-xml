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
package org.sonar.plugins.xml.checks;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;
import static org.fest.assertions.Assertions.assertThat;

public class XmlFileTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  public void assertDelta(String content, int offsetDeltaExpected, int lineDeltaExpected) throws Exception {
    String fileName = "char_before_prolog.xml";
    File file = tmpFolder.newFile(fileName);
    FileUtils.write(file, content);
    DefaultInputFile inputFile = new DefaultInputFile(fileName)
      .setLanguage(Xml.KEY)
      .setType(InputFile.Type.MAIN)
      .setAbsolutePath(file.getAbsolutePath());
    DefaultFileSystem localFS = new DefaultFileSystem(new File(file.getParent()));
    localFS.add(inputFile).setWorkDir(tmpFolder.newFolder());

    XmlFile xmlFile = new XmlFile(inputFile, localFS);

    assertThat(xmlFile.getOffsetDelta()).isEqualTo(offsetDeltaExpected);
    assertThat(xmlFile.getLineDelta()).isEqualTo(lineDeltaExpected);
  }

  @Test
  public void test_no_delta() throws Exception {
    assertDelta("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>", 0, 0);
  }

  @Test
  public void test_no_delta_if_no_prolog() throws Exception {
    assertDelta(" <tag/>", 0, 0);
    assertDelta("<tag/>", 0, 0);
  }

  @Test
  public void test_no_line_delta() throws Exception {
    assertDelta("abc<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>", 3, 0);
    assertDelta(" <?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>", 1, 0);
  }

  @Test
  public void test_line_delta() throws Exception {
    assertDelta("abc\n<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>", 4, 1);
    assertDelta("\n\n<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>", 2, 2);
  }
}
