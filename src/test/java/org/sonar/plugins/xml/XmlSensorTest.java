/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
 * dev@sonar.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonar.plugins.xml;

import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.RuleParam;

import java.io.File;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class XmlSensorTest extends AbstractXmlPluginTester {

  private void createXPathRuleForPomFiles(RulesProfile rulesProfile) {
    ActiveRule activeRule = rulesProfile.getActiveRule("Xml", "XPathCheck");
    assertNotNull(activeRule);

    RuleParam expressionParam = activeRule.getRule().getParam("expression");
    assertNotNull(expressionParam);

    expressionParam = activeRule.getRule().getParam("filePattern");
    assertNotNull(expressionParam);

    activeRule.setParameter("filePattern", "**/pom.xml");
    activeRule.setParameter("expression", "//dependency/version");
  }

  @Test
  public void testSensor() throws Exception {

    File pomFile = new File(XmlSensorTest.class.getResource("/pom.xml").toURI());

    final Project project = loadProjectFromPom(pomFile);

    MockSensorContext sensorContext = new MockSensorContext();
    RulesProfile rulesProfile = createStandardRulesProfile();
    createXPathRuleForPomFiles(rulesProfile);

    XmlSensor sensor = new XmlSensor(rulesProfile, new SimpleRuleFinder(rulesProfile), new Settings());

    assertTrue(sensor.shouldExecuteOnProject(project));

    sensor.analyse(project, sensorContext);

    assertTrue("Should have found 1 violation", sensorContext.getViolations().size() > 0);
  }

  @Test
  public void testFileFilter() throws Exception {

    File pomFile = new File(XmlSensorTest.class.getResource("/pom.xml").toURI());

    final Project project = loadProjectFromPom(pomFile);

    MockSensorContext sensorContext = new MockSensorContext();
    RulesProfile rulesProfile = createStandardRulesProfile();
    createXPathRuleForPomFiles(rulesProfile);

    XmlSensor sensor = new XmlSensor(rulesProfile, new SimpleRuleFinder(rulesProfile), new Settings());

    assertTrue(sensor.shouldExecuteOnProject(project));

    // add an additional file filter
    project.getConfiguration().addProperty(XmlPlugin.INCLUDE_FILE_FILTER, "**/not found");

    sensor.analyse(project, sensorContext);

    assertTrue("Should have found 0 violation", sensorContext.getViolations().size() == 0);
  }
}
