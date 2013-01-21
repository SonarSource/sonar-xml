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
package org.sonar.plugins.xml;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
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
import org.sonar.plugins.xml.parsers.LineCountParser;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LineCountSensorTest {

  @Test
  public void testLineCountParser() throws IOException {
    LineCountParser parser = new LineCountParser();
    int numCommentLines = parser.countLinesOfComment(FileUtils.openInputStream(new File("src/test/resources/checks/generic/catalog.xml")));
    assertEquals(1, numCommentLines);
  }

  @Test
  public void failingLineCountParser() throws IOException {
    LineCountParser parser = new LineCountParser();
    String filename = "src/test/resources/checks/generic/header.html";

    int numCommentLines = parser.countLinesOfComment(FileUtils.openInputStream(new File(filename)));
    assertEquals(0, numCommentLines);
  }

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
    project.getPom().addCompileSourceRoot("src/test/resources/checks/generic");

    assertTrue(lineCountSensor.shouldExecuteOnProject(project));
    lineCountSensor.analyse(project, sensorContext);

    for (Resource resource : sensorContext.getResources()) {
      assertTrue(sensorContext.getMeasure(resource, CoreMetrics.LINES).getIntValue() > 10);
      assertTrue(sensorContext.getMeasure(resource, CoreMetrics.NCLOC).getIntValue() > 10);
    }
  }
}
