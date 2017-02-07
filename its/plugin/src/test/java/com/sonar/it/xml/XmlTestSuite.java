/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.it.xml;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import com.sonar.orchestrator.locator.FileLocation;
import java.io.File;
import java.util.List;
import javax.annotation.CheckForNull;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonarqube.ws.WsMeasures;
import org.sonarqube.ws.WsMeasures.Measure;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.measure.ComponentWsRequest;

import static java.util.Collections.singletonList;

@RunWith(Suite.class)
@SuiteClasses({
  XmlTest.class,
  SchemaCheckTest.class})
public class XmlTestSuite {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Orchestrator.builderEnv()
    .addPlugin(FileLocation.byWildcardMavenFilename(new File("../../sonar-xml-plugin/target"), "sonar-xml-plugin-*.jar"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/sonar-way-it-profile_xml.xml"))
    .restoreProfileAtStartup(FileLocation.ofClasspath("/empty-profile.xml"))
    .build();

  public static SonarRunner createSonarRunner() {
    SonarRunner build = SonarRunner.create();
    // xhtml has been removed from default file suffixes (SONARXML-5)
    build.setProperty("sonar.xml.file.suffixes", ".xml,.xhtml");
    return build;
  }

  public static boolean is_at_least_sonar_5_1() {
    return ORCHESTRATOR.getConfiguration().getSonarVersion().isGreaterThanOrEquals("5.1");
  }

  @CheckForNull
  static Measure getMeasure(String componentKey, String metricKey) {
    WsMeasures.ComponentWsResponse response = newWsClient().measures().component(new ComponentWsRequest()
      .setComponentKey(componentKey)
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
    return WsClientFactories.getDefault().newClient(HttpConnector.newBuilder()
      .url(ORCHESTRATOR.getServer().getUrl())
      .build());
  }

}
