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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.plugins.xml.checks.XmlFile;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LineCounterTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();
  private FileLinesContextFactory fileLinesContextFactory;
  FileLinesContext fileLinesContext;

  @Before
  public void setUp() throws Exception {
    fileLinesContextFactory = mock(FileLinesContextFactory.class);
    fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
  }

  @Test
  public void test_simple_file() {
    DefaultFileSystem localFS = new DefaultFileSystem(new File("src/test/resources/parsers/linecount/"));
    DefaultInputFile inputFile = createInputFile("simple.xml");
    localFS.add(inputFile);

    SensorContext context = mock(SensorContext.class);

    LineCounter.analyse(context, fileLinesContextFactory, new XmlFile(inputFile, localFS), Charsets.UTF_8);

    // No empty line at end of file
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.LINES), eq(18.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.NCLOC), eq(15.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.COMMENT_LINES), eq(1.0));
  }

  @Test
  public void test_complex() {
    DefaultFileSystem localFS = new DefaultFileSystem(new File("src/test/resources/parsers/linecount/"));
    DefaultInputFile inputFile = createInputFile("complex.xml");
    localFS.add(createInputFile("complex.xml"));

    SensorContext context = mock(SensorContext.class);

    LineCounter.analyse(context, fileLinesContextFactory, new XmlFile(inputFile, localFS), Charsets.UTF_8);

    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.COMMENT_LINES), eq(12.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.LINES), eq(40.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.NCLOC), eq(21.0));
  }

  @Test // SONARXML-19
  public void test_file_with_char_before_prolog() throws Exception {
    DefaultFileSystem localFS = new DefaultFileSystem(new File("src/test/resources/parsers/linecount/"));
    DefaultInputFile inputFile = createInputFile("char_before_prolog.xml");
    localFS.add(createInputFile("char_before_prolog.xml"));
    localFS.setWorkDir(tmpFolder.newFolder());

    SensorContext context = mock(SensorContext.class);

    LineCounter.analyse(context, fileLinesContextFactory, new XmlFile(inputFile, localFS), Charsets.UTF_8);

    verify_line_data();

    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.COMMENT_LINES), eq(1.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.LINES), eq(21.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.NCLOC), eq(15.0));
  }

  private void verify_line_data() {
    verify(fileLinesContext, atLeastOnce()).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), eq(1), eq(0));
    verify(fileLinesContext, atLeastOnce()).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), eq(2), eq(0));
    verify(fileLinesContext, atLeastOnce()).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), eq(3), eq(1));
    verify(fileLinesContext, atLeastOnce()).setIntValue(eq(CoreMetrics.NCLOC_DATA_KEY), eq(6), eq(1));
    verify(fileLinesContext).setIntValue(eq(CoreMetrics.COMMENT_LINES_DATA_KEY), eq(1), eq(0));
    verify(fileLinesContext).setIntValue(eq(CoreMetrics.COMMENT_LINES_DATA_KEY), eq(5), eq(1));
  }

  private DefaultInputFile createInputFile(String name) {
    return new DefaultInputFile(name)
      .setLanguage(Xml.KEY)
      .setType(InputFile.Type.MAIN)
      .setAbsolutePath(new File("src/test/resources/parsers/linecount/" + name).getAbsolutePath());
  }

}
