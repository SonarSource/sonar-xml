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
package org.sonar.plugins.xml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.plugins.xml.checks.XmlFile;
import org.sonar.plugins.xml.compat.CompatibleInputFile;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.plugins.xml.compat.CompatibilityHelper.wrap;

public class LineCounterTest {

  private static final String MODULE_KEY = "modulekey";

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();
  private FileLinesContextFactory fileLinesContextFactory;
  private FileLinesContext fileLinesContext;

  @Before
  public void setUp() throws Exception {
    fileLinesContextFactory = mock(FileLinesContextFactory.class);
    fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
  }

  @Test
  public void test_simple_file() throws IOException {
    verifyMetrics("simple.xml", 15, 1);
  }

  @Test
  public void test_complex() throws IOException {
    verifyMetrics("complex.xml", 21, 12);
  }

  private void verifyMetrics(String filename, int ncloc, int commentLines) throws IOException {
    File moduleBaseDir = new File("src/test/resources/parsers/linecount");
    CompatibleInputFile inputFile = createInputFile(moduleBaseDir.toPath(), filename);
    String componentKey = getComponentKey(filename);

    DefaultFileSystem localFS = new DefaultFileSystem(moduleBaseDir);
    localFS.setWorkDir(tmpFolder.newFolder());

    SensorContextTester context = SensorContextTester.create(moduleBaseDir);
    LineCounter.analyse(context, fileLinesContextFactory, new XmlFile(inputFile, localFS));

    // No empty line at end of file
    assertThat(context.measure(componentKey, CoreMetrics.NCLOC).value()).isEqualTo(ncloc);
    assertThat(context.measure(componentKey, CoreMetrics.COMMENT_LINES).value()).isEqualTo(commentLines);
  }

  private String getComponentKey(String filename) {
    return MODULE_KEY + ":" + filename;
  }

  @Test // SONARXML-19
  public void test_file_with_char_before_prolog() throws Exception {
    verifyMetrics("char_before_prolog.xml", 15, 1);

    verify(fileLinesContext, atLeastOnce()).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), eq(1), eq(0));
    verify(fileLinesContext, atLeastOnce()).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), eq(2), eq(0));
    verify(fileLinesContext, atLeastOnce()).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), eq(3), eq(1));
    verify(fileLinesContext, atLeastOnce()).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), eq(6), eq(1));
    verify(fileLinesContext).setIntValue(eq(CoreMetrics.COMMENT_LINES_DATA_KEY), eq(1), eq(0));
    verify(fileLinesContext).setIntValue(eq(CoreMetrics.COMMENT_LINES_DATA_KEY), eq(5), eq(1));
  }

  private CompatibleInputFile createInputFile(Path moduleBaseDir, String name) {
    return wrap(new DefaultInputFile(MODULE_KEY, name)
      .setModuleBaseDir(moduleBaseDir)
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(StandardCharsets.UTF_8));
  }

}
