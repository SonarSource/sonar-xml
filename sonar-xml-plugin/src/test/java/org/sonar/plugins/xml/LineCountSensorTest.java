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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LineCountSensorTest {

  private Project project;
  private DefaultFileSystem fs;
  private FileLinesContextFactory fileLinesContextFactory;

  @Before
  public void setUp() throws Exception {
    project = new Project("");
    fs = new DefaultFileSystem(new File("tmp/"));

    fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
  }

  @Test
  public void should_execute_on_xml_project() {
    LineCountSensor sensor = new LineCountSensor(fs, fileLinesContextFactory);

    // No XML file
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    // Has XML file
    fs.add(createInputFile("file.xml"));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void test_simple_file() {
    DefaultFileSystem localFS = new DefaultFileSystem(new File("src/test/resources/parsers/linecount/"));
    localFS.add(createInputFile("simple.xml"));

    SensorContext context = mock(SensorContext.class);

    new LineCountSensor(localFS, fileLinesContextFactory).analyse(project, context);


    // No empty line at end of file
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.LINES), eq(18.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.NCLOC), eq(15.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.COMMENT_LINES), eq(1.0));
  }

  @Test
  public void test_complex() {
    DefaultFileSystem localFS = new DefaultFileSystem(new File("src/test/resources/parsers/linecount/"));
    localFS.add(createInputFile("complex.xml"));

    SensorContext context = mock(SensorContext.class);

    new LineCountSensor(localFS, fileLinesContextFactory).analyse(project, context);

    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.COMMENT_LINES), eq(12.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.LINES), eq(40.0));
    verify(context).saveMeasure(any(InputFile.class), eq(CoreMetrics.NCLOC), eq(21.0));
  }

  private DefaultInputFile createInputFile(String name) {
    return new DefaultInputFile(name)
      .setLanguage(Xml.KEY)
      .setType(InputFile.Type.MAIN)
      .setAbsolutePath(new File("src/test/resources/parsers/linecount/" + name).getAbsolutePath());
  }

  @Test
  public void test_toString() throws Exception {
    assertThat(new LineCountSensor(fs, fileLinesContextFactory).toString()).isEqualTo(LineCountSensor.class.getSimpleName());

  }
}
