/*
 * SonarQube XML Plugin
 * Copyright (C) 2013-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package com.sonar.it.xml;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.client.GetRequest;

import static com.sonar.it.xml.XmlTestSuite.newWsClient;
import static org.assertj.core.api.Assertions.assertThat;

class ByteOrderMarkTest {
  private static final String PROJECT_KEY = "byte-order-mark";

  @RegisterExtension
  private static final OrchestratorExtension ORCHESTRATOR = XmlTestSuite.ORCHESTRATOR;

  @Test
  void test() {
    ORCHESTRATOR.getServer().provisionProject(PROJECT_KEY, PROJECT_KEY);
    ORCHESTRATOR.getServer().associateProjectToQualityProfile(PROJECT_KEY, "xml", "it-profile");
    ORCHESTRATOR.executeBuild(XmlTestSuite.createSonarScanner()
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
