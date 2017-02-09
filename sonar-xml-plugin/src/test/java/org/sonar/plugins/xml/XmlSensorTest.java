/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
package org.sonar.plugins.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.language.Xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    defaultInputFile.initMetadata(new FileMetadata().readMetadata(defaultInputFile.file(), StandardCharsets.UTF_8));
    return defaultInputFile;
  }

  @Test
  public void should_analyze_file_with_its_own_encoding() throws IOException {
    Charset fileSystemCharset = StandardCharsets.UTF_8;
    Charset fileCharset = StandardCharsets.UTF_16;

    Path moduleBaseDir = temporaryFolder.newFolder().toPath();
    SensorContextTester context = SensorContextTester.create(moduleBaseDir);

    DefaultFileSystem fileSystem = new DefaultFileSystem(moduleBaseDir);
    fileSystem.setEncoding(fileSystemCharset);
    context.setFileSystem(fileSystem);

    String filename = "utf16.xml";
    try (BufferedWriter writer = Files.newBufferedWriter(moduleBaseDir.resolve(filename), fileCharset)) {
      writer.write("<?xml version=\"1.0\" encoding=\"utf-16\" standalone=\"yes\"?>\n");
      writer.write("<tag></tag>");
    }

    String modulekey = "modulekey";
    DefaultInputFile defaultInputFile = new DefaultInputFile(modulekey, filename)
      .setModuleBaseDir(moduleBaseDir)
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(fileCharset);
    defaultInputFile.initMetadata(new FileMetadata().readMetadata(defaultInputFile.file(), fileCharset));
    fileSystem.add(defaultInputFile);

    ActiveRules activeRules = new ActiveRulesBuilder()
      .create(ruleKey)
      .activate()
      .build();
    CheckFactory checkFactory = new CheckFactory(activeRules);

    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));
    sensor = new XmlSensor(fileSystem, checkFactory, fileLinesContextFactory);
    sensor.analyse(context);

    String componentKey = modulekey + ":" + filename;
    assertThat(context.measure(componentKey, CoreMetrics.LINES).value()).isEqualTo(2);
  }
}
