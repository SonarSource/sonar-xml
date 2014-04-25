/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleParam;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.plugins.xml.checks.AbstractXmlCheck;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.checks.XPathCheck;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XmlSensorTest extends AbstractXmlPluginTester {

  private void createXPathRuleForPomFiles(RulesProfile rulesProfile) {
    Rule xpathRule = getRule("XPathCheck", XPathCheck.class);
    rulesProfile.activateRule(xpathRule, null);
    ActiveRule activeRule = rulesProfile.getActiveRule(xpathRule);
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
    Project project = mock(Project.class);
    when(project.getLanguageKey()).thenReturn(Xml.KEY);
    addProjectFileSystem(project, "src/test/resources/src/");

    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.of(new File("src/test/resources/src/pom.xml")));

    MockSensorContext sensorContext = new MockSensorContext();
    RulesProfile rulesProfile = createStandardRulesProfile();
    createXPathRuleForPomFiles(rulesProfile);

    XmlSensor sensor = new XmlSensor(rulesProfile, fs);

    assertTrue(sensor.shouldExecuteOnProject(project));

    sensor.analyse(project, sensorContext);

    assertTrue("Should have found 1 violation", sensorContext.getViolations().size() > 0);
  }

  private Rule getRule(String ruleKey, Class<? extends AbstractXmlCheck> checkClass) {

    AnnotationRuleParser parser = new AnnotationRuleParser();
    List<Rule> rules = parser.parse(CheckRepository.REPOSITORY_KEY, Arrays.asList(new Class[]{checkClass}));
    for (Rule rule : rules) {
      if (rule.getKey().equals(ruleKey)) {
        return rule;
      }
    }
    return null;
  }


  /**
   * This is unavoidable in order to be compatible with sonarqube 4.2
   */
  private void addProjectFileSystem(Project project, String srcDir) {
    ProjectFileSystem fs = mock(ProjectFileSystem.class);
    when(fs.getSourceDirs()).thenReturn(Arrays.asList(new File(srcDir)));

    when(project.getFileSystem()).thenReturn(fs);
  }
}
