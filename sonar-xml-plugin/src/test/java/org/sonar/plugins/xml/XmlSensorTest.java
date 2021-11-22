/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.Metadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.internal.apachecommons.io.FileUtils;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LogAndArguments;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.plugins.xml.checks.TabCharacterCheck;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnableRuleMigrationSupport
class XmlSensorTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  private DefaultFileSystem fs;
  private XmlSensor sensor;
  private SensorContextTester context;

  private static final RuleKey NEW_LINE_RULE_KEY = RuleKey.of(Xml.REPOSITORY_KEY, "S2321");
  private static final String PARSING_ERROR_CHECK_KEY = "S2260";
  private static final RuleKey PARSING_ERROR_RULE_KEY = RuleKey.of(Xml.REPOSITORY_KEY, PARSING_ERROR_CHECK_KEY);
  private static final RuleKey TAB_CHARACTER_RULE_KEY = RuleKey.of(Xml.REPOSITORY_KEY, TabCharacterCheck.RULE_KEY);

  @Test
  @Timeout(value = 12000, unit = TimeUnit.MILLISECONDS)
  void testPerformance() throws Exception {
    init();
    File smallXmlFile = createXmlFile(20000, "smallFile.xml");
    fs.add(createInputFile(Paths.get(smallXmlFile.getParent()), smallXmlFile.getName(), StandardCharsets.UTF_8));
    long timeSmallFile = measureTimeToAnalyzeFile();

    init();
    File bigXmlFile = createXmlFile(40000, "bigFile.xml");
    fs.add(createInputFile(Paths.get(bigXmlFile.getParent()), bigXmlFile.getName(), StandardCharsets.UTF_8));
    long timeBigFile = measureTimeToAnalyzeFile();

    assertThat(timeBigFile < (2.5 * timeSmallFile)).isTrue();
  }

  @Test
  void test_analysis_cancellation() throws Exception {
    init();
    fs.add(createInputFile("src/pom.xml"));

    context.setCancelled(true);
    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void test_nothing_is_executed_if_no_file() throws Exception {
    init();

    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void test_descriptor() throws Exception {
    init();
    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
    sensor.describe(sensorDescriptor);
    assertThat(sensorDescriptor.name()).isEqualTo("XML Sensor");
    assertThat(sensorDescriptor.languages()).containsOnly(Xml.KEY);
  }

  /**
   * Expect issue for rule: S2321
   */
  @Test
  void test_sensor() throws Exception {
    init();
    DefaultInputFile inputFile = createInputFile("src/pom.xml");
    fs.add(inputFile);

    sensor.execute(context);

    assertThat(context.allIssues()).extracting("ruleKey").containsOnly(NEW_LINE_RULE_KEY);

    // other measures
    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(16);
    assertThat(context.highlightingTypeAt(inputFile.key(), 4, 9)).containsOnly(TypeOfText.KEYWORD);
  }

  @Test
  void test_sensor_should_not_fail() throws Exception {
    init();
    DefaultInputFile inputFile = createInputFile("src/shouldNotFail.xml");
    fs.add(inputFile);

    sensor.execute(context);

    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC).value()).isEqualTo(3);
  }

  @Test
  void test_sensor_in_sonarlint_context() throws Exception {
    init();
    DefaultInputFile inputFile = createInputFile("src/pom.xml");
    fs.add(inputFile);

    context.setRuntime(SonarRuntimeImpl.forSonarLint(Version.create(4, 1)));
    sensor.execute(context);

    assertThat(context.allIssues()).extracting("ruleKey").containsOnly(NEW_LINE_RULE_KEY);

    // no other measures
    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC)).isNull();
    assertThat(context.highlightingTypeAt(inputFile.key(), 4, 9)).isEmpty();
  }

  /**
   * Expect issue for rule: TabCharacterCheck
   */
  @Test
  void should_execute_new_rules() throws Exception {
    init();
    fs.add(createInputFile("src/tabsEverywhere.xml"));

    sensor.execute(context);

    assertThat(context.allIssues()).extracting("ruleKey").containsOnly(TAB_CHARACTER_RULE_KEY);
  }

  @Test
  void failing_rules_should_not_report_parse_exception() throws Exception {
    init(true);

    sensor.runCheck(context, new SonarXmlCheck() {
      @Override
      public void scanFile(XmlFile file) {
        throw new IllegalStateException("failing systematically");
      }
    }, RuleKey.of("xml", "S666"), XmlFile.create(createInputFile("src/tabsEverywhere.xml")));

    assertThat(context.allIssues()).isEmpty();
    assertThat(logTester.getLogs()).isNotEmpty();

    List<LogAndArguments> errors = logTester.getLogs(LoggerLevel.ERROR);
    assertThat(errors).hasSize(1);
    assertThat(errors.get(0).getRawMsg()).startsWith("Unable to execute rule xml:S666");
  }

  /**
   * SONARXML-19
   * Expect issue for rule: S2321
   */
  @Test
  void should_execute_on_file_with_chars_before_prolog() throws Exception {
    init();
    fs.add(createInputFile("src/pom_with_chars_before_prolog_and_missing_new_line.xml"));

    sensor.execute(context);

    assertThat(context.allIssues()).extracting("ruleKey").containsOnly(NEW_LINE_RULE_KEY);
  }

  /**
   * Has issue for rule S2321, but should not be reported.
   * As rule ParsingErrorCheck is enabled, this test should report a parsing issue. It should also log a trace.
   */
  @Test
  void should_not_execute_test_on_corrupted_file_and_should_raise_parsing_issue() throws Exception {
    init(true);
    fs.add(createInputFile("src/wrong-ampersand.xhtml"));

    sensor.execute(context);

    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo(PARSING_ERROR_CHECK_KEY);

    assertLog("Unable to analyse file .*wrong-ampersand.*", true);
    assertLog("Cause: org.xml.sax.SAXParseException.* Element type \"as\\.length\" must be followed by either attribute specifications, .*", true);
  }

  /**
   * Has issue for rule S2321, but should not be reported.
   * As rule ParsingErrorCheck is not enabled, this test should not report any issue. It should log a trace instead.
   */
  @Test
  void should_not_execute_test_on_corrupted_file_and_should_not_raise_parsing_issue() throws Exception {
    init();
    fs.add(createInputFile("src/wrong-ampersand.xhtml"));

    sensor.execute(context);

    assertThat(context.allIssues()).isEmpty();

    assertLog("Unable to analyse file .*wrong-ampersand.*", true);
    assertLog("Cause: org.xml.sax.SAXParseException.* Element type \"as\\.length\" must be followed by either attribute specifications, .*", true);
  }

  private void init() throws Exception {
    init(false);
  }

  private void init(boolean activateParsingErrorCheck) throws Exception {
    File moduleBaseDir = new File("src/test/resources");
    context = SensorContextTester.create(moduleBaseDir);

    fs = new DefaultFileSystem(moduleBaseDir);
    fs.setWorkDir(temporaryFolder.newFolder().toPath());

    ActiveRulesBuilder activeRuleBuilder = new ActiveRulesBuilder()
      .addRule(new NewActiveRule.Builder().setRuleKey(NEW_LINE_RULE_KEY).build())
      .addRule(new NewActiveRule.Builder().setRuleKey(TAB_CHARACTER_RULE_KEY).build());

    if (activateParsingErrorCheck) {
      activeRuleBuilder.addRule(new NewActiveRule.Builder().setRuleKey(PARSING_ERROR_RULE_KEY).build());
    }

    CheckFactory checkFactory = new CheckFactory(activeRuleBuilder.build());

    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

    sensor = new XmlSensor(fs, checkFactory, fileLinesContextFactory);
  }

  @Test
  void should_analyze_file_with_its_own_encoding() throws Exception {
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

    fileSystem.add(createInputFile(moduleBaseDir, filename, fileCharset));

    ActiveRules activeRules = new ActiveRulesBuilder()
      .addRule(new NewActiveRule.Builder().setRuleKey(NEW_LINE_RULE_KEY).build())
      .build();
    CheckFactory checkFactory = new CheckFactory(activeRules);

    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));
    sensor = new XmlSensor(fileSystem, checkFactory, fileLinesContextFactory);
    sensor.execute(context);

    String componentKey = "modulekey:" + filename;
    assertThat(context.measure(componentKey, CoreMetrics.NCLOC).value()).isEqualTo(2);
  }

  private void assertLog(String expected, boolean isRegexp) {
    if (isRegexp) {
      Condition<String> regexpMatches = new Condition<String>(log -> Pattern.compile(expected).matcher(log).matches(), "");
      assertThat(logTester.logs())
        .filteredOn(regexpMatches)
        .as("None of the lines in " + logTester.logs() + " matches regexp [" + expected + "], but one line was expected to match")
        .isNotEmpty();
    } else {
      assertThat(logTester.logs()).contains(expected);
    }
  }

  private File createXmlFile(int numberOfTags, String fileName) {
    try {
      File file = temporaryFolder.newFile(fileName);
      StringBuilder str = new StringBuilder("<?xml version=\"1.0\"?><root>\n");
      IntStream.range(0, numberOfTags).forEach(iteration -> str.append("<tag1 attr=\"val1\">text</tag1>\n"));
      str.append("</root>");
      FileUtils.write(file, str.toString(), StandardCharsets.UTF_8);
      return file;
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create " + fileName);
    }
  }

  private long measureTimeToAnalyzeFile() {
    long t1 = System.currentTimeMillis();
    sensor.execute(context);
    return System.currentTimeMillis() - t1;
  }

  private static DefaultInputFile createInputFile(String filename) throws Exception {
    return createInputFile(Paths.get("src/test/resources"), filename, StandardCharsets.UTF_8);
  }

  private static DefaultInputFile createInputFile(Path moduleBaseDir, String filename, Charset charset) throws Exception {
    DefaultInputFile inputFile = TestInputFileBuilder.create("modulekey", filename)
      .setModuleBaseDir(moduleBaseDir)
      .setType(Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(charset)
      .build();
    Metadata metadata = new FileMetadata(s -> {
    }).readMetadata(new FileInputStream(inputFile.file()), inputFile.charset(), inputFile.absolutePath());
    return inputFile.setMetadata(metadata);
  }

}
