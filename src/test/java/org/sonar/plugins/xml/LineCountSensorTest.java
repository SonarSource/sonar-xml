/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.parsers.LineCountParser;

public class LineCountSensorTest {

  @Test
  public void testLineCountSensor() {
    LineCountSensor lineCountSensor = new LineCountSensor();

    MockSensorContext sensorContext = new MockSensorContext();
    Project project = new Project("");
    project.setConfiguration(new PropertiesConfiguration());
    DefaultProjectFileSystem filesystem = new DefaultProjectFileSystem(project);
    project.setFileSystem(filesystem);
    project.setPom(new MavenProject());
    project.setLanguageKey(Xml.KEY);
    project.getPom().addCompileSourceRoot("src/test/resources/checks/generic");

    assertTrue(lineCountSensor.shouldExecuteOnProject(project));
    lineCountSensor.analyse(project, sensorContext);

    for (Resource resource : sensorContext.getResources()) {
      assertTrue("Missing Comment line in " + resource.getKey(), sensorContext.getMeasure(resource, CoreMetrics.COMMENT_LINES)
          .getIntValue() > 0);
      assertTrue(sensorContext.getMeasure(resource, CoreMetrics.LINES).getIntValue() > 10);
      assertTrue(sensorContext.getMeasure(resource, CoreMetrics.NCLOC).getIntValue() > 10);
    }
  }

  @Test
  public void testLineCountParser() throws IOException {
    LineCountParser parser = new LineCountParser();
    int numCommentLines = parser.countLinesOfComment(FileUtils.openInputStream(new File("src/test/resources/checks/generic/catalog.xml")));
    assertEquals(1, numCommentLines);
  }
}
