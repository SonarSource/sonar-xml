/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.Version;
import org.sonar.plugins.xml.checks.CheckList;
import org.sonar.plugins.xml.checks.ParsingErrorCheck;
import org.sonar.plugins.xml.checks.ReadAnalysisConfiguration;
import org.sonarsource.analyzer.commons.ProgressReport;
import org.sonarsource.analyzer.commons.xml.ParseException;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;

public class XmlSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(XmlSensor.class);

  private static final RuleKey PARSING_ERROR_RULE_KEY = RuleKey.of(Xml.REPOSITORY_KEY, ParsingErrorCheck.RULE_KEY);

  private final Checks<Object> checks;
  private final boolean parsingErrorCheckEnabled;
  private final FileSystem fileSystem;
  private final FilePredicate mainFilesPredicate;
  private final SonarRuntime sonarRuntime;
  private final FileLinesContextFactory fileLinesContextFactory;
  private final Configuration configuration;

  public XmlSensor(SonarRuntime sonarRuntime, FileSystem fileSystem, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory) {
    this(sonarRuntime, fileSystem, checkFactory, fileLinesContextFactory, null);
  }

  public XmlSensor(SonarRuntime sonarRuntime, FileSystem fileSystem, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory,
    @Nullable Configuration configuration) {
    this.sonarRuntime = sonarRuntime;
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.checks = checkFactory.create(Xml.REPOSITORY_KEY).addAnnotatedChecks(CheckList.getCheckClasses());
    this.parsingErrorCheckEnabled = this.checks.of(PARSING_ERROR_RULE_KEY) != null;
    this.fileSystem = fileSystem;
    this.mainFilesPredicate = fileSystem.predicates()
      .and(
        fileSystem.predicates().hasType(InputFile.Type.MAIN),
        fileSystem.predicates().or(fileSystem.predicates().hasLanguage(Xml.KEY)),
        fileSystem.predicates().doesNotMatchPathPattern("**/*.cls-meta.xml"));
    this.configuration = configuration;
  }

  @Override
  public void execute(SensorContext context) {
    List<InputFile> inputFiles = new ArrayList<>();
    fileSystem.inputFiles(mainFilesPredicate).forEach(inputFiles::add);

    if (inputFiles.isEmpty()) {
      return;
    }

    boolean isSonarLintContext = context.runtime().getProduct() == SonarProduct.SONARLINT;

    ProgressReport progressReport = new ProgressReport("Report about progress of XML Analyzer", TimeUnit.SECONDS.toMillis(10));
    progressReport.start(inputFiles.stream().map(InputFile::toString).collect(Collectors.toList()));

    boolean cancelled = false;
    try {
      for (InputFile inputFile : inputFiles) {
        if (context.isCancelled()) {
          cancelled = true;
          break;
        }
        scanFile(context, inputFile, isSonarLintContext);
        progressReport.nextFile();
      }
    } finally {
      if (!cancelled) {
        progressReport.stop();
      } else {
        progressReport.cancel();
      }
    }
  }

  private void scanFile(SensorContext context, InputFile inputFile, boolean isSonarLintContext) {
    try {
      XmlFile xmlFile = XmlFile.create(inputFile);
      if (!isSonarLintContext) {
        LineCounter.analyse(context, fileLinesContextFactory, xmlFile);
        XmlHighlighting.highlight(context, xmlFile);
      }
      if (configuration != null) {
        configureChecks();
      }
      runChecks(context, xmlFile);
    } catch (Exception e) {
      if (e instanceof ParseException && Xml.isConfigFile(inputFile)) {
        // it's not mandatory for a "*.config" file to have an XML format.
        return;
      }
      processParseException(e, context, inputFile);
    }
  }

  private void configureChecks() {
    checks.all().stream()
      .filter(ReadAnalysisConfiguration.class::isInstance)
      .map(ReadAnalysisConfiguration.class::cast)
      .forEach(check -> check.readAnalysisConfiguration(configuration));
  }

  private void runChecks(SensorContext context, XmlFile newXmlFile) {
    checks.all().stream()
      .map(SonarXmlCheck.class::cast)
      // checks.ruleKey(check) is never null because "check" is part of "checks.all()"
      .forEach(check -> runCheck(context, check, checks.ruleKey(check), newXmlFile));
  }

  // Visible for testing
  void runCheck(SensorContext context, SonarXmlCheck check, RuleKey ruleKey, XmlFile newXmlFile) {
    try {
      check.scanFile(context, ruleKey, newXmlFile);
    } catch (Exception e) {
      logFailingRule(ruleKey, newXmlFile.getInputFile().uri(), e);
    }
  }

  private static void logFailingRule(RuleKey rule, URI fileLocation, Exception e) {
    LOG.error("Unable to execute rule {} on {}", rule, fileLocation, e);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(Xml.KEY)
      .onlyOnFileType(InputFile.Type.MAIN)
      .name("XML Sensor");
    processesFilesIndependently(descriptor);
  }

  private void processesFilesIndependently(SensorDescriptor descriptor) {
    if ((sonarRuntime.getProduct() != SonarProduct.SONARQUBE)
      || !sonarRuntime.getApiVersion().isGreaterThanOrEqual(Version.create(9, 3))) {
      return;
    }
    try {
      Method method = descriptor.getClass().getMethod("processesFilesIndependently");
      method.invoke(descriptor);
    } catch (ReflectiveOperationException e) {
      LOG.warn("Could not call SensorDescriptor.processesFilesIndependently() method", e);
    }
  }

  private void processParseException(Exception e, SensorContext context, InputFile inputFile) {
    reportAnalysisError(e, context, inputFile);

    LOG.warn("Unable to analyse file {};", inputFile.uri());
    LOG.debug("Cause: {}", e.getMessage());

    if (parsingErrorCheckEnabled) {
      createParsingErrorIssue(e, context, inputFile);
    }
  }

  private static void reportAnalysisError(Exception e, SensorContext context, InputFile inputFile) {
    context.newAnalysisError()
      .onFile(inputFile)
      .message(e.getMessage())
      .save();
  }

  private static void createParsingErrorIssue(Exception e, SensorContext context, InputFile inputFile) {
    NewIssue newIssue = context.newIssue();
    NewIssueLocation primaryLocation = newIssue.newLocation()
      .message("Parse error: " + e.getMessage())
      .on(inputFile);
    newIssue
      .forRule(PARSING_ERROR_RULE_KEY)
      .at(primaryLocation)
      .save();
  }

}
