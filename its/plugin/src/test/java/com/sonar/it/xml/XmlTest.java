/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.it.xml;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import java.io.File;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.WsMeasures.Measure;

import static com.sonar.it.xml.XmlTestSuite.getMeasure;
import static com.sonar.it.xml.XmlTestSuite.getMeasureAsDouble;
import static org.fest.assertions.Assertions.assertThat;

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
    assertThat(getProjectMeasureAsDouble("comment_lines_density")).isEqualTo(1.4);
    assertThat(getProjectMeasureAsDouble("comment_lines")).isEqualTo(4);
    assertThat(getProjectMeasureAsDouble("files")).isEqualTo(4);
    assertThat(getProjectMeasureAsDouble("violations")).isEqualTo(19);
  }

  @Test
  public void should_be_compatible_with_DevCockpit() {
    assertThat(getFileMeasure("ncloc_data").getValue())
      .contains(";7=1")
      .doesNotContain("8=1")
      .doesNotContain("9=1")
      .doesNotContain("10=1")
      .contains(";11=1");

    assertThat(getFileMeasure("comment_lines_data").getValue())
      .contains("9=1")
      .doesNotContain("10=1");
  }

  @Test // SONARXML-19
  public void should_correctly_count_lines_when_char_before_prolog() {
    assertThat(getFileMeasureAsDouble("lines")).isEqualTo(14);
  }

  private static void inspectProject(String name) throws InterruptedException {
    orchestrator.getServer().provisionProject(name, name);
    orchestrator.getServer().associateProjectToQualityProfile(name, "xml", "it-profile");

    SonarRunner build = XmlTestSuite.createSonarRunner()
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
