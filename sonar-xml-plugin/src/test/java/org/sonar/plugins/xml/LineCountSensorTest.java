/*
 * SonarQube XML Plugin
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
package org.sonar.plugins.xml;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.xml.language.Xml;

public class LineCountSensorTest {

  private Project project;
  private DefaultFileSystem fs;

  @Before
  public void setUp() throws Exception {
    project = new Project("");
    fs = new DefaultFileSystem(new File("tmp/"));
  }

  @Test
  public void should_execute_on_javascript_project() {
    LineCountSensor sensor = new LineCountSensor(fs);

    // No XML file
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    // Has XML file
    fs.add(createInputFile("file.xml"));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void testLineCountSensor() {
    fs.add(createInputFile("complex.xml"));
    fs.add(createInputFile("simple.xml"));

    MockSensorContext sensorContext = new MockSensorContext();

    new LineCountSensor(fs).analyse(project, sensorContext);

    for (Resource resource : sensorContext.getResources()) {
      if (resource.getKey().equals("complex.xml")) {
        assertThat(sensorContext.getMeasure(resource, CoreMetrics.LINES).getIntValue()).isEqualTo(26);
        // TODO SONARPLUGINS-2623
        // assertThat(sensorContext.getMeasure(resource, CoreMetrics.NCLOC).getIntValue()).isEqualTo(21);

      } else if (resource.getKey().equals("simple.xml")) {
        assertThat(sensorContext.getMeasure(resource, CoreMetrics.LINES).getIntValue()).isEqualTo(18);
        assertThat(sensorContext.getMeasure(resource, CoreMetrics.NCLOC).getIntValue()).isEqualTo(15);

      } else {
        fail("Unexpected resource: " + resource.getKey());
      }
    }
  }

  private DefaultInputFile createInputFile(String name) {
    return new DefaultInputFile(name)
      .setLanguage(Xml.KEY)
      .setType(InputFile.Type.MAIN)
      .setAbsolutePath(new File("src/test/resources/parsers/linecount/" + name).getAbsolutePath());
  }

  @Test
  public void test_toString() throws Exception {
    assertThat(new LineCountSensor(fs).toString()).isEqualTo(LineCountSensor.class.getSimpleName());

  }
}
