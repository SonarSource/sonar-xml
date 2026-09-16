/*
 * SonarQube XML Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import com.sonar.orchestrator.container.Edition;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.Location;
import com.sonar.orchestrator.locator.MavenLocation;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;

import static com.sonar.it.xml.XmlTestSuite.DEFAULT_SQ_VERSION;
import static com.sonar.it.xml.XmlTestSuite.SQ_VERSION_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

class ProfileRegistrarTest {

  @RegisterExtension
  static final OrchestratorExtension ORCHESTRATOR = OrchestratorExtension.builderEnv()
    .useDefaultAdminCredentialsForBuilds(true)
    .setEdition(Edition.ENTERPRISE_LW)
    .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
    .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-xml-plugin/target"), "sonar-xml-plugin-*.jar"))
    .addPlugin(getTestPlugin())
    .restoreProfileAtStartup(FileLocation.ofClasspath("/sonar-way-it-profile_xml.xml"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/empty-profile.xml"))
    .build();

  private static Location getTestPlugin() {
    // Always use the test plugin that was built on local machine
    String projectVersion = System.getProperty("projectVersion");
    return MavenLocation.of("org.sonarsource.xml", "test-plugin", projectVersion);
  }

  private static WsClient newWsClient() {
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
      .url(ORCHESTRATOR.getServer().getUrl())
      .build());
  }

  @Test
  void testPluginRegistersRuleInDefaultXmlProfile() {
    var wsClient = newWsClient();

    // First, query the quality profiles to find the profile key for Xml's "Sonar way" profile
    var profileSearchRequest = new org.sonarqube.ws.client.qualityprofiles.SearchRequest()
      .setLanguage("xml");

    var profileResponse = wsClient.qualityprofiles().search(profileSearchRequest);

    var xmlProfile = profileResponse.getProfilesList().stream()
      .filter(profile -> "Sonar way".equals(profile.getName()) && profile.getIsBuiltIn())
      .findFirst()
      .orElseThrow(() -> new AssertionError("Built-in 'Sonar way' profile not found for xml language"));

    var profileKey = xmlProfile.getKey();

    // Query the active rules in the built-in "Sonar way" profile for xml language using the profile key
    var rulesSearchRequest = new org.sonarqube.ws.client.rules.SearchRequest()
      .setLanguages(List.of("xml"))
      .setQprofile(profileKey)
      .setActivation("true");

    var rulesResponse = wsClient.rules().search(rulesSearchRequest);

    // Verify that the profile contains the TEST001 rule from xml-test repository
    // This rule is registered by the TestProfileRegistrar in the test-plugin
    var testRules = rulesResponse.getRulesList().stream()
      .filter(rule -> "xml-test:TEST001".equals(rule.getKey()))
      .toList();

    assertThat(testRules)
      .as("Rule xml-test:TEST001 should be registered in the default xml profile by TestProfileRegistrar")
      .hasSize(1);

    var testRule = testRules.get(0);
    assertThat(testRule.getKey()).isEqualTo("xml-test:TEST001");
    assertThat(testRule.getRepo()).isEqualTo("xml-test");
  }

}
