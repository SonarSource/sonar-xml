/*
 * XML :: IT
 * Copyright (C) 2013 ${owner}
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonar.it.xml;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class XmlTest {

  @ClassRule
  public static Orchestrator orchestrator = XmlTestSuite.ORCHESTRATOR;

  private static Sonar sonar;
  private static final String PROJECT = "org.codehaus.sonar:example-xml-sonar-runner";

  @BeforeClass
  public static void inspect() throws Exception {
    sonar = orchestrator.getServer().getWsClient();
    inspectProject("xml-sonar-runner");
  }

  @Test
  public void testBaseMetrics() {
    assertThat(getProjectMeasure("lines").getValue()).isEqualTo(317);
    assertThat(getProjectMeasure("ncloc").getValue()).isEqualTo(282);
    assertThat(getProjectMeasure("comment_lines_density").getValue()).isEqualTo(1.4);
    assertThat(getProjectMeasure("comment_lines").getIntValue()).isEqualTo(4);
    assertThat(getProjectMeasure("files").getValue()).isEqualTo(4);
    assertThat(getProjectMeasure("violations").getIntValue()).isEqualTo(46);
  }

  private static void inspectProject(String name) throws InterruptedException {
    orchestrator.getServer().provisionProject(name, name);
    orchestrator.getServer().associateProjectToQualityProfile(name, "xml", "it-profile");

    SonarRunner build = XmlTestSuite.createSonarRunner()
      .setProjectDir(new File("projects/" + name));
    orchestrator.executeBuild(build);
  }

  private Measure getProjectMeasure(String metricKey) {
    return getMeasure(PROJECT, metricKey);
  }

  private Measure getMeasure(String resource, String metricKey) {
    Resource res = sonar.find(ResourceQuery.createForMetrics(resource, metricKey));
    if (res == null) {
      return null;
    }
    return res.getMeasure(metricKey);
  }

}
