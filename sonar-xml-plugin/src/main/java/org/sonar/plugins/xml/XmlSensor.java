/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import java.net.URI;
import java.util.List;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.internal.google.common.annotations.VisibleForTesting;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.xml.checks.AbstractXmlCheck;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.checks.XmlFile;
import org.sonar.plugins.xml.checks.XmlIssue;
import org.sonar.plugins.xml.checks.XmlSourceCode;
import org.sonar.plugins.xml.highlighting.HighlightingData;
import org.sonar.plugins.xml.highlighting.XMLHighlighting;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.newchecks.NewXmlCheckList;
import org.sonar.plugins.xml.newchecks.ParsingErrorCheck;
import org.sonar.plugins.xml.newparser.NewXmlFile;
import org.sonar.plugins.xml.newparser.checks.NewXmlCheck;

public class XmlSensor implements Sensor {

  private static final Logger LOG = Loggers.get(XmlSensor.class);

  private static final RuleKey PARSING_ERROR_RULE_KEY = RuleKey.of(Xml.REPOSITORY_KEY, ParsingErrorCheck.RULE_KEY);

  private final Checks<Object> checks;
  private final boolean reportingParsingErrors;
  private final FileSystem fileSystem;
  private final FilePredicate mainFilesPredicate;
  private final FileLinesContextFactory fileLinesContextFactory;

  public XmlSensor(FileSystem fileSystem, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.checks = checkFactory.create(Xml.REPOSITORY_KEY)
      .addAnnotatedChecks((Iterable<?>) CheckRepository.getCheckClasses())
      .addAnnotatedChecks((Iterable<?>) NewXmlCheckList.getCheckClasses());
    this.reportingParsingErrors = this.checks.of(PARSING_ERROR_RULE_KEY) != null;
    this.fileSystem = fileSystem;
    this.mainFilesPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Xml.KEY));
  }

  @Override
  public void execute(SensorContext context) {

    for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {
      XmlFile xmlFile = new XmlFile(inputFile, fileSystem);
      try {
        NewXmlFile newXmlFile = NewXmlFile.create(inputFile);
        LineCounter.analyse(context, fileLinesContextFactory, newXmlFile);
        runChecks(context, xmlFile, newXmlFile);
        saveSyntaxHighlighting(context, XMLHighlighting.highlight(newXmlFile), inputFile);
      } catch (Exception e) {
        processParseException(e, context, inputFile);
      }
    }
  }

  private void runChecks(SensorContext context, XmlFile xmlFile, NewXmlFile newXmlFile) {
    XmlSourceCode sourceCode = new XmlSourceCode(xmlFile);

    // Do not execute any XML rule when an XML file is corrupted (SONARXML-13)
    if (sourceCode.parseSource()) {
      // FIXME we should drop this part once SONARXML-78 has been completed
      checks.all().stream()
        .filter(AbstractXmlCheck.class::isInstance)
        .map(AbstractXmlCheck.class::cast)
        .forEach(check -> runCheck(check, sourceCode));
      saveIssues(context, sourceCode);
    }

    checks.all().stream()
      .filter(NewXmlCheck.class::isInstance)
      .map(NewXmlCheck.class::cast)
      .forEach(check -> runCheck(context, check, newXmlFile));
  }

  private void runCheck(AbstractXmlCheck check, XmlSourceCode sourceCode) {
    try {
      check.setRuleKey(checks.ruleKey(check));
      check.validate(sourceCode);
    } catch (Exception e) {
      logFailingRule(check.getRuleKey().rule(), sourceCode.getInputFile().uri(), e);
    }
  }

  @VisibleForTesting
  static void runCheck(SensorContext context, NewXmlCheck check, NewXmlFile newXmlFile) {
    try {
      check.scanFile(context, newXmlFile);
    } catch (Exception e) {
      logFailingRule(check.ruleKey(), newXmlFile.getInputFile().uri(), e);
    }
  }

  private static void logFailingRule(String rule, URI fileLocation, Exception e) {
    logError(String.format("Unable to execute rule %s on %s", rule, fileLocation), e);
  }

  private static void logError(String message, Exception e) {
    LOG.warn(message);
    LOG.debug("Cause: {}", e.getMessage());
  }

  private static void saveSyntaxHighlighting(SensorContext context, List<HighlightingData> highlightingDataList, InputFile inputFile) {
    NewHighlighting highlighting = context.newHighlighting().onFile(inputFile);

    for (HighlightingData highlightingData : highlightingDataList) {
      highlightingData.highlight(highlighting);
    }
    highlighting.save();
  }

  protected void saveIssues(SensorContext context, XmlSourceCode sourceCode) {
    for (XmlIssue xmlIssue : sourceCode.getXmlIssues()) {
      NewIssue newIssue = context.newIssue().forRule(xmlIssue.getRuleKey());
      NewIssueLocation location = newIssue.newLocation()
        .on(sourceCode.getInputFile())
        .message(xmlIssue.getMessage());
      if (xmlIssue.getLine() != null) {
        location.at(sourceCode.getInputFile().selectLine(xmlIssue.getLine()));
      }
      newIssue.at(location).save();
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .onlyOnLanguage(Xml.KEY)
      .name("XML Sensor");
  }

  private void processParseException(Exception e, SensorContext context, InputFile inputFile) {
    reportAnalysisError(e, context, inputFile);

    logError(String.format("Unable to analyse file %s", inputFile.uri()), e);

    if (reportingParsingErrors) {
      reportParsingException(e, context, inputFile);
    }
  }

  private static void reportAnalysisError(Exception e, SensorContext context, InputFile inputFile) {
    context.newAnalysisError()
      .onFile(inputFile)
      .message(e.getMessage())
      .save();
  }

  private static void reportParsingException(Exception e, SensorContext context, InputFile inputFile) {
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
