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
import org.apache.commons.collections.ListUtils;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.sonar.api.component.Perspective;
import org.sonar.api.component.Perspectives;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
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
import org.sonar.plugins.xml.checks.XmlSourceCode;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class XmlSensorTest extends AbstractXmlPluginTester {

  @org.junit.Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private void createXPathRuleForPomFiles(RulesProfile rulesProfile) {
    Rule xpathRule = getRule("XPathCheck", XPathCheck.class);
    rulesProfile.activateRule(xpathRule, null);
    ActiveRule activeRule = rulesProfile.getActiveRule(xpathRule);
    assertNotNull(activeRule);

    RuleParam expressionParam = activeRule.getRule().getParam("expression");
    assertNotNull(expressionParam);

    expressionParam = activeRule.getRule().getParam("filePattern");
    assertNotNull(expressionParam);

    activeRule.setParameter("filePattern", "**/pom*.xml");
    activeRule.setParameter("expression", "//dependency/version");
  }

  @Test
  public void should_execute_on_javascript_project() {
    Project project = new Project("key");
    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    XmlSensor sensor = new XmlSensor(mock(RulesProfile.class), fs, mock(ResourcePerspectives.class));

    when(fs.files(any(FileQuery.class))).thenReturn(ListUtils.EMPTY_LIST);
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    when(fs.files(Mockito.any(FileQuery.class))).thenReturn(ImmutableList.of(new File("/tmp")));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  @Test
  public void testSensor() throws Exception {
    Project project = mock(Project.class);
    when(project.getLanguageKey()).thenReturn(Xml.KEY);
    addProjectFileSystem(project, "src/test/resources/src/");

    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.of(new File("src/test/resources/src/pom.xml")));
    when(fs.sourceCharset()).thenReturn(Charset.defaultCharset());
    when(fs.workingDir()).thenReturn(temporaryFolder.newFolder("temp"));

    MockSensorContext sensorContext = new MockSensorContext();
    RulesProfile rulesProfile = createStandardRulesProfile();
    createXPathRuleForPomFiles(rulesProfile);

    Issuable issuable = mock(Issuable.class);
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);
    when(perspectives.as(any(Class.class), any(org.sonar.api.resources.File.class))).thenReturn(issuable);
    XmlSensor sensor = spy(new XmlSensor(rulesProfile, fs, mock(ResourcePerspectives.class)));

    sensor.analyse(project, sensorContext);

    verify(sensor, atLeastOnce()).saveIssue(any(XmlSourceCode.class));
  }

  /**
   * SONARXML-19
   */
  @Test
  public void should_execute_on_file_with_chars_before_prolog() throws Exception {
    Project project = mock(Project.class);
    when(project.getLanguageKey()).thenReturn(Xml.KEY);
    addProjectFileSystem(project, "src/test/resources/src/");

    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.of(new File("src/test/resources/src/pom_with_chars_before_prolog.xml")));
    when(fs.sourceCharset()).thenReturn(Charset.defaultCharset());
    when(fs.workingDir()).thenReturn(temporaryFolder.newFolder("temp"));

    MockSensorContext sensorContext = new MockSensorContext();
    RulesProfile rulesProfile = createStandardRulesProfile();
    createXPathRuleForPomFiles(rulesProfile);

    Issuable issuable = mock(Issuable.class);
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);
    when(perspectives.as(any(Class.class), any(org.sonar.api.resources.File.class))).thenReturn(issuable);

    XmlSensor sensor = spy(new XmlSensor(rulesProfile, fs, perspectives));

    sensor.analyse(project, sensorContext);

    verify(sensor, atLeastOnce()).saveIssue(any(XmlSourceCode.class));
  }

  @Test
  public void should_not_execute_test_on_corrupted_file() throws Exception {
    Project project = mock(Project.class);
    when(project.getLanguageKey()).thenReturn(Xml.KEY);
    addProjectFileSystem(project, "src/test/resources/src/");

    ModuleFileSystem fs = mock(ModuleFileSystem.class);
    when(fs.files(any(FileQuery.class))).thenReturn(ImmutableList.of(new File("src/test/resources/checks/generic/wrong-ampersand.xhtml")));
    when(fs.sourceCharset()).thenReturn(Charset.defaultCharset());
    when(fs.workingDir()).thenReturn(temporaryFolder.newFolder("temp"));

    MockSensorContext sensorContext = new MockSensorContext();
    RulesProfile rulesProfile = createStandardRulesProfile();
    createXPathRuleForPomFiles(rulesProfile);

    Issuable issuable = mock(Issuable.class);
    ResourcePerspectives perspectives = mock(ResourcePerspectives.class);
    when(perspectives.as(any(Class.class), any(org.sonar.api.resources.File.class))).thenReturn(issuable);

    XmlSensor sensor = spy(new XmlSensor(rulesProfile, fs, perspectives));

    sensor.analyse(project, sensorContext);

    verify(sensor, never()).saveIssue(any(XmlSourceCode.class));
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
