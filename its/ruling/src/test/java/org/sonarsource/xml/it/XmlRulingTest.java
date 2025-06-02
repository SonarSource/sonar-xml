/*
 * SonarQube XML Plugin
 * Copyright (C) 2018-2025 SonarSource SA
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
package org.sonarsource.xml.it;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import com.sonar.orchestrator.locator.MavenLocation;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.rule.RuleStatus;
import org.sonarqube.ws.Qualityprofiles.SearchWsResponse.QualityProfile;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.qualityprofiles.ActivateRuleRequest;
import org.sonarqube.ws.client.qualityprofiles.SearchRequest;
import org.sonarqube.ws.client.rules.CreateRequest;
import org.sonarsource.analyzer.commons.ProfileGenerator;

import static com.sonar.orchestrator.container.Server.ADMIN_LOGIN;
import static com.sonar.orchestrator.container.Server.ADMIN_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;

class XmlRulingTest {

  private static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  private static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";
  private static final String LANGUAGE = "xml";
  private static final String QUALITY_PROFILE_NAME = "rules";

  @RegisterExtension
  static final OrchestratorExtension ORCHESTRATOR = OrchestratorExtension.builderEnv()
    .useDefaultAdminCredentialsForBuilds(true)
    .setSonarVersion(Optional.ofNullable(System.getProperty(SQ_VERSION_PROPERTY)).orElse(DEFAULT_SQ_VERSION))
    .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-xml-plugin/target"), "sonar-xml-plugin-*.jar"))
    .addPlugin(MavenLocation.of("org.sonarsource.sonar-lits-plugin", "sonar-lits-plugin", "0.11.0.2659"))
    .build();

  @BeforeAll
  static void beforeAll() {
    ProfileGenerator.RulesConfiguration rulesConfiguration = new ProfileGenerator.RulesConfiguration();

    // generate a profile called "rules"
    File profile = ProfileGenerator.generateProfile(ORCHESTRATOR.getServer().getUrl(), LANGUAGE, LANGUAGE, rulesConfiguration, Collections.emptySet());
    ORCHESTRATOR.getServer().restoreProfile(FileLocation.of(profile));

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
  void test() throws Exception {
    ORCHESTRATOR.getServer().provisionProject("project", "project");
    ORCHESTRATOR.getServer().associateProjectToQualityProfile("project", LANGUAGE, QUALITY_PROFILE_NAME);
    File litsDifferencesFile = FileLocation.of("target/differences").getFile();
    SonarScanner build = SonarScanner.create(FileLocation.of("../sources/projects").getFile())
      .setProperty("sonar.scanner.skipJreProvisioning", "true")
      .setProjectKey("project")
      .setProjectName("project")
      .setProjectVersion("1")
      .setSourceEncoding("UTF-8")
      .setSourceDirs(".")
      .setProperty("sonar.lits.dump.old", FileLocation.of("src/test/resources/expected").getFile().getAbsolutePath())
      .setProperty("sonar.lits.dump.new", FileLocation.of("target/actual").getFile().getAbsolutePath())
      .setProperty("sonar.cpd.exclusions", "**/*")
      .setProperty("sonar.lits.differences", litsDifferencesFile.getAbsolutePath());
    ORCHESTRATOR.executeBuild(build);

    assertThat(Files.readAllLines(litsDifferencesFile.toPath(), StandardCharsets.UTF_8)).isEmpty();
  }

  private static void createTemplateRule(String templateRuleKey, String newRuleKey, String newRuleParameters) {

    WsClient adminWSClient = newAdminWsClient();
    adminWSClient.rules().create(new CreateRequest()
      .setName(newRuleKey)
      .setMarkdownDescription(newRuleKey)
      .setSeverity("INFO")
      .setStatus(RuleStatus.READY.name())
      .setTemplateKey(LANGUAGE + ":" + templateRuleKey)
      .setCustomKey(newRuleKey)
      .setPreventReactivation("true")
      .setParams(Arrays.asList(newRuleParameters.split(";"))));

    QualityProfile qualityProfile = adminWSClient.qualityprofiles().search(new SearchRequest()).getProfilesList().stream()
      .filter(qp -> qp.getLanguage().equals(LANGUAGE))
      .filter(qp -> qp.getName().equals(QUALITY_PROFILE_NAME))
      .findFirst().orElseThrow(() -> new IllegalStateException(String.format("Could not find quality profile '%s' for language '%s' ", QUALITY_PROFILE_NAME, LANGUAGE)));
    String profileKey = qualityProfile.getKey();

    adminWSClient.qualityprofiles().activateRule(new ActivateRuleRequest()
      .setKey(profileKey)
      .setRule(LANGUAGE + ":" + newRuleKey)
      .setSeverity("INFO"));
  }

  private static WsClient newAdminWsClient() {
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
      .url(ORCHESTRATOR.getServer().getUrl())
      .credentials(ADMIN_LOGIN, ADMIN_PASSWORD)
      .build());
  }
}
