/*
 * Copyright (C) 2013-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.it.xml;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarRunner;
import com.sonar.orchestrator.locator.FileLocation;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({XmlTest.class})
public class XmlTestSuite {

  private static final String PLUGIN_KEY = "xml";

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Orchestrator.builderEnv()
    .addPlugin(PLUGIN_KEY)
    .setMainPluginKey(PLUGIN_KEY)
    .restoreProfileAtStartup(FileLocation.ofClasspath("/sonar-way-it-profile_xml.xml"))
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

  public static boolean is_plugin_at_least(String version) {
    return ORCHESTRATOR.getConfiguration().getPluginVersion(PLUGIN_KEY).isGreaterThanOrEquals(version);
  }
}
