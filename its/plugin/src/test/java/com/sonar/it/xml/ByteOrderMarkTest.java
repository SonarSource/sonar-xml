/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.it.xml;

import com.google.common.collect.ImmutableMap;
import com.sonar.orchestrator.Orchestrator;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.SonarClient;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

public class ByteOrderMarkTest {

  private static final String PROJECT_KEY = "byte-order-mark";

  @ClassRule
  public static Orchestrator orchestrator = XmlTestSuite.ORCHESTRATOR;

  @Test
  public void test() throws Exception {
    orchestrator.getServer().provisionProject(PROJECT_KEY, PROJECT_KEY);
    orchestrator.getServer().associateProjectToQualityProfile(PROJECT_KEY, "xml", "it-profile");
    orchestrator.executeBuild(XmlTestSuite.createSonarRunner()
      .setProjectDir(new File("projects/" + PROJECT_KEY))
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_KEY)
      .setProjectVersion("1.0")
      .setSourceEncoding("utf-8")
      .setSourceDirs("."));

    SonarClient sonarClient = orchestrator.getServer().adminWsClient();
    Object fileKey = PROJECT_KEY + ":utf8-bom.xml";
    String highlighting = sonarClient.get("/api/sources/show", ImmutableMap.of("key", fileKey));
    assertThat(highlighting).contains("<span class=\\\"k\\\">&lt;?xml</span>");
  }

}
