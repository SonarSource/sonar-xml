/*
 * SonarQube XML Plugin
 * Copyright (C) 2013-2025 SonarSource SA
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

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.sonarqube.ws.Measures;
import org.sonarqube.ws.Measures.Measure;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.measures.ComponentRequest;

import static java.util.Collections.singletonList;

@Suite
@SelectClasses({
  RequiredForLanguagesTest.class,
  ByteOrderMarkTest.class,
  XmlTest.class,
  SonarLintTest.class})
public class XmlTestSuite {

  private static final String SQ_VERSION_PROPERTY = "sonar.runtimeVersion";
  private static final String DEFAULT_SQ_VERSION = "LATEST_RELEASE";

  @RegisterExtension
  static final OrchestratorExtension ORCHESTRATOR = OrchestratorExtension.builderEnv()
    .useDefaultAdminCredentialsForBuilds(true)
    .setSonarVersion(System.getProperty(SQ_VERSION_PROPERTY, DEFAULT_SQ_VERSION))
    .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-xml-plugin/target"), "sonar-xml-plugin-*.jar"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/sonar-way-it-profile_xml.xml"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/empty-profile.xml"))
    .build();

  static SonarScanner createSonarScanner() {
    SonarScanner build = SonarScanner.create();
    // xhtml has been removed from default file suffixes (SONARXML-5)
    build.setProperty("sonar.xml.file.suffixes", ".xml,.xhtml");
    return build;
  }

  @CheckForNull
  static Measure getMeasure(String componentKey, String metricKey) {
    Measures.ComponentWsResponse response = newWsClient().measures().component(new ComponentRequest()
      .setComponent(componentKey)
      .setMetricKeys(singletonList(metricKey)));
    List<Measure> measures = response.getComponent().getMeasuresList();
    return measures.size() == 1 ? measures.get(0) : null;
  }

  @CheckForNull
  static Double getMeasureAsDouble(String componentKey, String metricKey) {
    Measure measure = getMeasure(componentKey, metricKey);
    return (measure == null) ? null : Double.parseDouble(measure.getValue());
  }

  static WsClient newWsClient() {
    return newWsClient(null, null);
  }

  static WsClient newWsClient(@Nullable String login, @Nullable String password) {
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
      .url(ORCHESTRATOR.getServer().getUrl())
      .credentials(login, password)
      .build());
  }

}
