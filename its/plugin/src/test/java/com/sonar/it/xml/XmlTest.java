/*
 * SonarQube XML Plugin
 * Copyright (C) 2013-2022 SonarSource SA
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
package com.sonar.it.xml;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.Measures.Measure;

import static com.sonar.it.xml.XmlTestSuite.getMeasure;
import static com.sonar.it.xml.XmlTestSuite.getMeasureAsDouble;
import static org.assertj.core.api.Assertions.assertThat;

public class XmlTest {

  @ClassRule
  public static Orchestrator orchestrator = XmlTestSuite.ORCHESTRATOR;

  private static final String PROJECT = "xml-sonar-runner";
  private static final String FILE_TOKEN_PARSER = "xml-sonar-runner" + ":src/spring.xml";

  @BeforeClass
  public static void inspect() throws Exception {
    inspectProject(PROJECT);
  }

  @Test
  public void testBaseMetrics() {
    assertThat(getProjectMeasureAsDouble("lines")).isEqualTo(319);
    assertThat(getProjectMeasureAsDouble("ncloc")).isEqualTo(282);
    assertThat(getProjectMeasureAsDouble("comment_lines_density")).isEqualTo(3.1);
    assertThat(getProjectMeasureAsDouble("comment_lines")).isEqualTo(9);
    assertThat(getProjectMeasureAsDouble("files")).isEqualTo(4);
    assertThat(getProjectMeasureAsDouble("violations")).isEqualTo(13);
  }

  @Test
  public void should_be_compatible_with_DevCockpit() {
    assertThat(getFileMeasure("ncloc_data").getValue())
      .contains(";7=1")
      .doesNotContain("8=1")
      .doesNotContain("9=1")
      .doesNotContain("10=1")
      .contains(";11=1");
  }

  @Test // SONARXML-19
  public void should_correctly_count_lines_when_char_before_prolog() {
    assertThat(getFileMeasureAsDouble("lines")).isEqualTo(14);
  }

  private static void inspectProject(String name) throws InterruptedException {
    orchestrator.getServer().provisionProject(name, name);
    orchestrator.getServer().associateProjectToQualityProfile(name, "xml", "it-profile");

    SonarScanner build = XmlTestSuite.createSonarScanner()
      .setProjectName(name)
      .setProjectKey(name)
      .setProjectDir(new File("projects/" + name));
    orchestrator.executeBuild(build);
  }

  private Double getProjectMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(PROJECT, metricKey);
  }

  private Double getFileMeasureAsDouble(String metricKey) {
    return getMeasureAsDouble(FILE_TOKEN_PARSER, metricKey.trim());
  }

  private Measure getFileMeasure(String metricKey) {
    return getMeasure(FILE_TOKEN_PARSER, metricKey.trim());
  }

}
