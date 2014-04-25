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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.api.CoreProperties;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;
import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LineCountSensorTest {

  @Test
  public void testLineCountSensor() {
    Settings settings = new Settings(new PropertyDefinitions(XmlPlugin.class));
    settings.setProperty(CoreProperties.CORE_IMPORT_SOURCES_PROPERTY, true); // Default value is in CorePlugins

    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.of(
      new File("src/test/resources/parsers/linecount/complex.xml"),
      new File("src/test/resources/parsers/linecount/simple.xml")));

    LineCountSensor lineCountSensor = new LineCountSensor(settings, fs);

    MockSensorContext sensorContext = new MockSensorContext();
    Project project = mock(Project.class);
    when(project.getLanguageKey()).thenReturn(Xml.KEY);
    addProjectFileSystem(project, "src/test/resources/parsers/linecount/");

    assertTrue(lineCountSensor.shouldExecuteOnProject(project));
    lineCountSensor.analyse(project, sensorContext);

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

  /**
   * This is unavoidable in order to be compatible with sonarqube 4.2
   */
  private void addProjectFileSystem(Project project, String srcDir) {
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File(srcDir)));

    when(project.getFileSystem()).thenReturn(fs);
  }

}
