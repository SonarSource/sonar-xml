/*
 * SonarQube XML Plugin
 * Copyright (C) 2018-2021 SonarSource SA
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
package org.sonarsource.xml.it;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.Qualityprofiles.SearchWsResponse.QualityProfile;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.PostRequest;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.qualityprofiles.SearchRequest;
import org.sonarsource.analyzer.commons.ProfileGenerator;

import static com.sonar.orchestrator.container.Server.ADMIN_LOGIN;
import static com.sonar.orchestrator.container.Server.ADMIN_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

public class XmlRulingTest {

  private static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  private static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";
  private static final String LANGUAGE = "xml";
  private static final String QUALITY_PROFILE_NAME = "rules";

  @ClassRule
  public static Orchestrator orchestrator = Orchestrator.builderEnv()
    .setSonarVersion(Optional.ofNullable(System.getProperty(SQ_VERSION_PROPERTY)).orElse(DEFAULT_SQ_VERSION))
    .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-xml-plugin/target"), "sonar-xml-plugin-*.jar"))
    .addPlugin(MavenLocation.of("org.sonarsource.sonar-lits-plugin", "sonar-lits-plugin", "0.8.0.1209"))
    .build();

  @Before
  public void setUp() throws Exception {
    ProfileGenerator.RulesConfiguration rulesConfiguration = new ProfileGenerator.RulesConfiguration();

    // generate a profile called "rules"
    File profile = ProfileGenerator.generateProfile(orchestrator.getServer().getUrl(), LANGUAGE, LANGUAGE, rulesConfiguration, Collections.emptySet());
    orchestrator.getServer().restoreProfile(FileLocation.of(profile));

    createTemplateRule(
      "XPathCheck",
      "NumberDependencies",
      "expression=\"count(/project/dependencies/dependency)>0\";" +
        "message=\"Don't do that, dammit!\"");

    createTemplateRule(
      "XPathCheck",
      "checkstyleXPath",
      "expression=\"//checkstyle\";" +
        "message=\"Don't do that, dammit!\";");

    createTemplateRule(
      "S3417",
      "S3417_doNotUseCommonsBeanutils",
      "dependencyName=\"commons-beanutils:*\";");

    createTemplateRule(
      "S3417",
      "S3417_doNotUseJunitBefore4",
      "dependencyName=\"junit:junit\";version=\"*-3.9.9\"");
  }

  @Test
  public void test() throws Exception {
    orchestrator.getServer().provisionProject("project", "project");
    orchestrator.getServer().associateProjectToQualityProfile("project", LANGUAGE, QUALITY_PROFILE_NAME);
    File litsDifferencesFile = FileLocation.of("target/differences").getFile();
    SonarScanner build = SonarScanner.create(FileLocation.of("../sources/projects").getFile())
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty("dump.old", FileLocation.of("src/test/resources/expected").getFile().getAbsolutePath())
      .setProperty("dump.new", FileLocation.of("target/actual").getFile().getAbsolutePath())
      .setProperty("sonar.cpd.exclusions", "**/*")
      .setProperty("lits.differences", litsDifferencesFile.getAbsolutePath());
    orchestrator.executeBuild(build);

    assertThat(Files.readAllLines(litsDifferencesFile.toPath(), StandardCharsets.UTF_8)).isEmpty();
  }

  private static void createTemplateRule(String templateRuleKey, String newRuleKey, String newRuleParameters) {

    WsClient adminWSClient = newAdminWsClient();
    adminWSClient.wsConnector().call(new PostRequest("api/rules/create")
      .setParam("name", newRuleKey)
      .setParam("markdown_description", newRuleKey)
      .setParam("severity", "INFO")
      .setParam("status", "READY")
      .setParam("template_key", LANGUAGE + ":" + templateRuleKey)
      .setParam("custom_key", newRuleKey)
      .setParam("prevent_reactivation", "true")
      .setParam("params", newRuleParameters)).failIfNotSuccessful();

    QualityProfile qualityProfile = adminWSClient.qualityprofiles().search(new SearchRequest()).getProfilesList().stream()
      .filter(qp -> qp.getLanguage().equals(LANGUAGE))
      .filter(qp -> qp.getName().equals(QUALITY_PROFILE_NAME))
      .findFirst().orElseThrow(() -> new IllegalStateException(String.format("Could not find quality profile '%s' for language '%s' ", QUALITY_PROFILE_NAME, LANGUAGE)));
    String profileKey = qualityProfile.getKey();

    adminWSClient.wsConnector().call(new PostRequest("api/qualityprofiles/activate_rule")
      .setParam("key", profileKey)
      .setParam("rule", LANGUAGE + ":" + newRuleKey)
      .setParam("severity", "INFO")).failIfNotSuccessful();
  }

  private static WsClient newAdminWsClient() {
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
    .url(orchestrator.getServer().getUrl())
    .credentials(ADMIN_LOGIN, ADMIN_PASSWORD)
    .build());
  }
}
