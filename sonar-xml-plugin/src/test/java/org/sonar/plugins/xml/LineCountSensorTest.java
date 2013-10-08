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

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.CoreProperties;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Languages;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.xml.language.Xml;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LineCountSensorTest {

  @Test
  public void testLineCountSensor() {
    Settings settings = new Settings(new PropertyDefinitions(XmlPlugin.class));
    settings.setProperty(CoreProperties.CORE_IMPORT_SOURCES_PROPERTY, true); // Default value is in CorePlugins
    LineCountSensor lineCountSensor = new LineCountSensor(settings);

    MockSensorContext sensorContext = new MockSensorContext();
    Project project = new Project("");
    project.setConfiguration(new PropertiesConfiguration());
    project.setFileSystem(new DefaultProjectFileSystem(project, new Languages(new Xml())));
    project.setPom(new MavenProject());
    project.setLanguageKey(Xml.KEY);
    project.getPom().addCompileSourceRoot("src/test/resources/parsers/linecount");

    assertTrue(lineCountSensor.shouldExecuteOnProject(project));
    lineCountSensor.analyse(project, sensorContext);

    for (Resource resource : sensorContext.getResources()) {
      if (resource.getKey().equals("complex.xml")) {
        assertThat(sensorContext.getMeasure(resource, CoreMetrics.LINES).getIntValue()).isEqualTo(26);
        // TODO SONARPLUGINS-2623
        // assertThat(sensorContext.getMeasure(resource, CoreMetrics.NCLOC).getIntValue()).isEqualTo(21);
      }
      else if (resource.getKey().equals("simple.xml")) {
        assertThat(sensorContext.getMeasure(resource, CoreMetrics.LINES).getIntValue()).isEqualTo(18);
        assertThat(sensorContext.getMeasure(resource, CoreMetrics.NCLOC).getIntValue()).isEqualTo(15);
      }
      else {
        fail("Unexpected resource: " + resource.getKey());
      }
    }
  }
}
