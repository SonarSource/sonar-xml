/*
 * SonarQube XML Plugin
 * Copyright (C) 2013-2024 SonarSource SA
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sonar.orchestrator.Orchestrator;
import java.io.File;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.client.GetRequest;

import static com.sonar.it.xml.XmlTestSuite.newWsClient;
import static org.assertj.core.api.Assertions.assertThat;

public class ByteOrderMarkTest {

  private static final String PROJECT_KEY = "byte-order-mark";

  @ClassRule
  public static Orchestrator orchestrator = XmlTestSuite.ORCHESTRATOR;

  @Test
  public void test() throws Exception {
    orchestrator.getServer().provisionProject(PROJECT_KEY, PROJECT_KEY);
    orchestrator.getServer().associateProjectToQualityProfile(PROJECT_KEY, "xml", "it-profile");
    orchestrator.executeBuild(XmlTestSuite.createSonarScanner()
      .setProjectDir(new File("projects/" + PROJECT_KEY))
      .setProjectKey(PROJECT_KEY)
      .setProjectName(PROJECT_KEY)
      .setProjectVersion("1.0")
      .setSourceEncoding("utf-8")
      .setSourceDirs("."));

    String fileKey = PROJECT_KEY + ":utf8-bom.xml";
    String highlighting = newWsClient().wsConnector().call(new GetRequest("api/sources/show").setParam("key", fileKey))
      .failIfNotSuccessful()
      .content();

    JsonObject response = new Gson().fromJson(highlighting, JsonObject.class);
    String lineOneCode = response.get("sources").getAsJsonArray()
      .get(0).getAsJsonArray()
      .get(1).getAsString();

    assertThat(lineOneCode).startsWith("<span class=\"k\">&lt;?xml</span>");
  }

}
