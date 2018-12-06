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

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
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

  private final Checks<Object> checks;
  private final FileSystem fileSystem;
  private final FilePredicate mainFilesPredicate;
  private final FileLinesContextFactory fileLinesContextFactory;

  public XmlSensor(FileSystem fileSystem, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.checks = checkFactory.create(Xml.REPOSITORY_KEY)
      .addAnnotatedChecks((Iterable<?>) CheckRepository.getCheckClasses())
      .addAnnotatedChecks((Iterable<?>) NewXmlCheckList.getCheckClasses());
    this.fileSystem = fileSystem;
    this.mainFilesPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Xml.KEY));
  }

  @Override
  public void execute(SensorContext context) {
    BiConsumer<Exception, InputFile> parsingExceptionHandler = parsingExceptionHandler(context);

    for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {
      XmlFile xmlFile = new XmlFile(inputFile, fileSystem);
      try {
        NewXmlFile newXmlFile = NewXmlFile.create(inputFile);
        LineCounter.analyse(context, fileLinesContextFactory, newXmlFile);
        runChecks(context, xmlFile, newXmlFile);
      } catch (Exception e) {
        processParseException(e, context, inputFile, parsingExceptionHandler);
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
        .forEach(check -> {
          check.setRuleKey(checks.ruleKey(check));
          check.validate(sourceCode);
        });
      saveIssue(context, sourceCode);
    }

    checks.all().stream()
      .filter(NewXmlCheck.class::isInstance)
      .map(NewXmlCheck.class::cast)
      .forEach(check -> check.scanFile(context, newXmlFile));

    saveSyntaxHighlighting(context, XMLHighlighting.highlight(newXmlFile), xmlFile.getInputFile());
  }

  private static void saveSyntaxHighlighting(SensorContext context, List<HighlightingData> highlightingDataList, InputFile inputFile) {
    NewHighlighting highlighting = context.newHighlighting().onFile(inputFile);

    for (HighlightingData highlightingData : highlightingDataList) {
      highlightingData.highlight(highlighting);
    }
    highlighting.save();
  }

  protected void saveIssue(SensorContext context, XmlSourceCode sourceCode) {
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

  private static void processParseException(Exception e, SensorContext context, InputFile inputFile, BiConsumer<Exception, InputFile> parsingExceptionConsumer) {
    reportAnalysisError(e, context, inputFile);

    LOG.warn("Unable to analyse file {}", inputFile.uri());
    LOG.debug("Cause: {}", e.getMessage());

    parsingExceptionConsumer.accept(e, inputFile);
  }

  private static void reportAnalysisError(Exception e, SensorContext context, InputFile inputFile) {
    context.newAnalysisError()
      .onFile(inputFile)
      .message(e.getMessage())
      .save();
  }

  private BiConsumer<Exception, InputFile> parsingExceptionHandler(SensorContext context) {
    Optional<RuleKey> parsingErrorKey = getParsingErrorKey();
    if (parsingErrorKey.isPresent()) {
      return (e, i) -> reportParsingException(e, context, i, parsingErrorKey.get());
    }
    return (e, i) -> { /* do nothing */ };
  }

  private Optional<RuleKey> getParsingErrorKey() {
    return checks.all().stream()
      .filter(ParsingErrorCheck.class::isInstance)
      .map(ParsingErrorCheck.class::cast)
      .map(checks::ruleKey)
      .findFirst();
  }

  private static void reportParsingException(Exception e, SensorContext context, InputFile inputFile, RuleKey parsingErrorKey) {
    NewIssue newIssue = context.newIssue();
    NewIssueLocation primaryLocation = newIssue.newLocation()
      .message("Parse error: " + e.getMessage())
      .on(inputFile);
    newIssue
      .forRule(parsingErrorKey)
      .at(primaryLocation)
      .save();
  }

}
