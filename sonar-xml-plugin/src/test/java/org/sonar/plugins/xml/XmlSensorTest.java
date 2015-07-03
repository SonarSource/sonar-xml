/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
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

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.DefaultActiveRules;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.checks.XmlSourceCode;
import org.sonar.plugins.xml.language.Xml;

public class XmlSensorTest extends AbstractXmlPluginTester {

  @org.junit.Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private Project project;
  private DefaultFileSystem fs;
  private XmlSensor sensor;
  private SensorContext context;
  private ResourcePerspectives perspectives;

  @Before
  public void setUp() throws Exception {
    project = new Project("");
    context = mock(SensorContext.class);

    fs = new DefaultFileSystem(new File("src/test/resources/"));
    fs.setWorkDir(temporaryFolder.newFolder("temp"));

    CheckFactory checkFactory = new CheckFactory(new DefaultActiveRules(
      ImmutableList.of(new ActiveRulesBuilder().create(RuleKey.of(CheckRepository.REPOSITORY_KEY, "NewlineCheck")))));

    perspectives = mock(ResourcePerspectives.class);
    when(perspectives.as(any(Class.class), any(InputFile.class))).thenReturn(mock(Issuable.class));

    sensor = new XmlSensor(fs, perspectives, checkFactory);
  }

  @Test
  public void should_execute_on_javascript_project() {
    // No XML file
    assertThat(sensor.shouldExecuteOnProject(project)).isFalse();

    // Has XML file
    fs.add(createInputFile("file.xml"));
    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
  }

  /**
   * Expect issue for rule: NewlineCheck
   */
  @Test
  public void testSensor() throws Exception {
    fs.add(createInputFile("src/pom.xml"));

    sensor.analyse(new Project(""), context);

    verify(perspectives, atLeastOnce()).as(any(Class.class), any(InputFile.class));
  }

  /**
   * SONARXML-19
   *
   * Expect issue for rule: NewlineCheck
   */
  @Test
  public void should_execute_on_file_with_chars_before_prolog() throws Exception {
    fs.add(createInputFile("checks/generic/pom_with_chars_before_prolog.xml"));

    sensor.analyse(new Project(""), context);

    verify(perspectives, atLeastOnce()).as(any(Class.class), any(InputFile.class));
  }

  /**
   * Has issue for rule: NewlineCheck, but should not be reported
   */
  @Test
  public void should_not_execute_test_on_corrupted_file() throws Exception {
    fs.add(createInputFile("checks/generic/wrong-ampersand.xhtml"));

    sensor.analyse(new Project(""), context);

    verify(perspectives, never()).as(any(Class.class), any(InputFile.class));
  }

  private DefaultInputFile createInputFile(String name) {
    return new DefaultInputFile(name)
      .setLanguage(Xml.KEY)
      .setType(InputFile.Type.MAIN)
      .setAbsolutePath(new File("src/test/resources/" + name).getAbsolutePath());
  }

}
