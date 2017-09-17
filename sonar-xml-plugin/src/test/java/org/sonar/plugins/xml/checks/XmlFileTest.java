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
package org.sonar.plugins.xml.checks;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.plugins.xml.language.Xml;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.xml.compat.CompatibilityHelper.wrap;

public class XmlFileTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  public void assertDelta(String content, int offsetDeltaExpected, int lineDeltaExpected) throws Exception {
    String fileName = "char_before_prolog.xml";
    File file = tmpFolder.newFile(fileName);
    FileUtils.write(file, content, UTF_8);
    DefaultInputFile inputFile = new DefaultInputFile("modulekey", fileName)
      .setModuleBaseDir(file.getParentFile().toPath())
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(UTF_8);
    DefaultFileSystem localFS = new DefaultFileSystem(new File(file.getParent()));
    localFS.add(inputFile).setWorkDir(tmpFolder.newFolder());

    XmlFile xmlFile = new XmlFile(wrap(inputFile), localFS);

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
