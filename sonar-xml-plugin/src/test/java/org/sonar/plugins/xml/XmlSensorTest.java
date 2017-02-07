/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.google.common.base.Charsets;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.language.Xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class XmlSensorTest extends AbstractXmlPluginTester {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private DefaultFileSystem fs;
  private XmlSensor sensor;
  private SensorContextTester context;

  private final RuleKey ruleKey = RuleKey.of(CheckRepository.REPOSITORY_KEY, "NewlineCheck");

  @Before
  public void setUp() throws Exception {
    File moduleBaseDir = new File("src/test/resources");
    context = SensorContextTester.create(moduleBaseDir);

    fs = new DefaultFileSystem(moduleBaseDir);
    fs.setWorkDir(temporaryFolder.newFolder("temp"));

    ActiveRules activeRules = new ActiveRulesBuilder()
      .create(ruleKey)
      .activate()
      .build();
    CheckFactory checkFactory = new CheckFactory(activeRules);

    sensor = new XmlSensor(fs, checkFactory, mock(FileLinesContextFactory.class));
  }

  /**
   * Expect issue for rule: NewlineCheck
   */
  @Test
  public void testSensor() throws Exception {
    fs.add(createInputFile("src/pom.xml"));

    sensor.analyse(context);

    assertThat(context.allIssues()).extracting("ruleKey").containsOnly(ruleKey);
  }

  /**
   * SONARXML-19
   *
   * Expect issue for rule: NewlineCheck
   */
  @Test
  public void should_execute_on_file_with_chars_before_prolog() throws Exception {
    fs.add(createInputFile("checks/generic/pom_with_chars_before_prolog.xml"));

    sensor.analyse(context);

    assertThat(context.allIssues()).extracting("ruleKey").containsOnly(ruleKey);
  }

  /**
   * Has issue for rule: NewlineCheck, but should not be reported
   */
  @Test
  public void should_not_execute_test_on_corrupted_file() throws Exception {
    fs.add(createInputFile("checks/generic/wrong-ampersand.xhtml"));

    sensor.analyse(context);

    assertThat(context.allIssues()).isEmpty();
  }

  private DefaultInputFile createInputFile(String name) {
    DefaultInputFile defaultInputFile = new DefaultInputFile("modulekey", name)
      .setModuleBaseDir(Paths.get("src/test/resources"))
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(StandardCharsets.UTF_8);
    defaultInputFile.initMetadata(new FileMetadata().readMetadata(defaultInputFile.file(), Charsets.UTF_8));
    return defaultInputFile;
  }
}
