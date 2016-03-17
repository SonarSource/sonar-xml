/*
 * SonarQube XML Plugin
 * Copyright (C) 2013-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
