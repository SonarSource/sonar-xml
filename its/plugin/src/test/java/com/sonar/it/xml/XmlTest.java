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

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;

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
    // FIXME(Godin): SQ 5.1 computes correct value (SONAR-5077) and there is a bug in plugin (SONARXML-20)

    if (XmlTestSuite.is_plugin_at_least("1.3")) {
      assertThat(getProjectMeasure("lines").getValue()).isEqualTo(317);
      assertThat(getProjectMeasure("ncloc").getValue()).isEqualTo(282);
      assertThat(getProjectMeasure("comment_lines_density").getValue()).isEqualTo(2.1);
      assertThat(getProjectMeasure("comment_lines").getIntValue()).isEqualTo(6);
    } else {
      if (XmlTestSuite.is_at_least_sonar_5_1()) {
        assertThat(getProjectMeasure("lines").getValue()).isEqualTo(317);
      } else {
        assertThat(getProjectMeasure("lines").getValue()).isEqualTo(315);
      }
      assertThat(getProjectMeasure("ncloc").getValue()).isEqualTo(277);
      assertThat(getProjectMeasure("comment_lines_density").getValue()).isEqualTo(3.8);
      assertThat(getProjectMeasure("comment_lines").getIntValue()).isEqualTo(11);
    }

    assertThat(getProjectMeasure("files").getValue()).isEqualTo(4);
    assertThat(getProjectMeasure("violations").getIntValue()).isEqualTo(52);
  }

  private static void inspectProject(String name) throws InterruptedException {
    SonarRunner build = XmlTestSuite.createSonarRunner()
      .setProjectDir(new File("projects/" + name))
      .setProfile("it-profile");
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
