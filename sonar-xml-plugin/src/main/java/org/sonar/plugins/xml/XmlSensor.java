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

import com.google.common.annotations.VisibleForTesting;
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
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.plugins.xml.checks.AbstractXmlCheck;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.checks.XmlFile;
import org.sonar.plugins.xml.checks.XmlIssue;
import org.sonar.plugins.xml.checks.XmlSourceCode;
import org.sonar.plugins.xml.compat.CompatibleInputFile;
import org.sonar.plugins.xml.highlighting.HighlightingData;
import org.sonar.plugins.xml.highlighting.XMLHighlighting;
import org.sonar.plugins.xml.language.Xml;

import static org.sonar.plugins.xml.compat.CompatibilityHelper.wrap;

/**
 * XmlSensor provides analysis of xml files.
 *
 * @author Matthijs Galesloot
 */
public class XmlSensor implements Sensor {

  private final Checks<Object> checks;
  private final FileSystem fileSystem;
  private final FilePredicate mainFilesPredicate;
  private final FileLinesContextFactory fileLinesContextFactory;

  public XmlSensor(FileSystem fileSystem, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.checks = checkFactory.create(CheckRepository.REPOSITORY_KEY).addAnnotatedChecks(CheckRepository.getCheckClasses());
    this.fileSystem = fileSystem;
    this.mainFilesPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Xml.KEY));
  }

  public void analyse(SensorContext sensorContext) {
    execute(sensorContext);
  }

  private void computeLinesMeasures(SensorContext context, XmlFile xmlFile) {
    LineCounter.analyse(context, fileLinesContextFactory, xmlFile);
  }

  private void runChecks(SensorContext context, XmlFile xmlFile) {
    try {
      XmlSourceCode sourceCode = new XmlSourceCode(xmlFile);

      // Do not execute any XML rule when an XML file is corrupted (SONARXML-13)
      if (sourceCode.parseSource()) {
        for (Object check : checks.all()) {
          ((AbstractXmlCheck) check).setRuleKey(checks.ruleKey(check));
          ((AbstractXmlCheck) check).validate(sourceCode);
        }
        saveIssue(context, sourceCode);

        saveSyntaxHighlighting(context, new XMLHighlighting(xmlFile, fileSystem.encoding()).getHighlightingData(), xmlFile.getInputFile());
      }
    } catch (Exception e) {
      throw new IllegalStateException("Could not analyze the file " + xmlFile.getAbsolutePath(), e);
    }
  }

  private static void saveSyntaxHighlighting(SensorContext context, List<HighlightingData> highlightingDataList, InputFile inputFile) {
    NewHighlighting highlighting = context.newHighlighting().onFile(inputFile);

    for (HighlightingData highlightingData : highlightingDataList) {
      highlighting.highlight(highlightingData.startOffset(), highlightingData.endOffset(), highlightingData.highlightCode());
    }
    highlighting.save();
  }

  @VisibleForTesting
  protected void saveIssue(SensorContext context, XmlSourceCode sourceCode) {
    for (XmlIssue xmlIssue : sourceCode.getXmlIssues()) {
      NewIssue newIssue = context.newIssue().forRule(xmlIssue.getRuleKey());
      NewIssueLocation location = newIssue.newLocation()
        .on(sourceCode.getInputFile())
        .at(sourceCode.getInputFile().selectLine(xmlIssue.getLine()))
        .message(xmlIssue.getMessage());
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

  @Override
  public void execute(SensorContext context) {
    for (CompatibleInputFile inputFile : wrap(fileSystem.inputFiles(mainFilesPredicate), context)) {
      // TODO
      XmlFile xmlFile = new XmlFile(inputFile.wrapped(), fileSystem);

      computeLinesMeasures(context, xmlFile);
      runChecks(context, xmlFile);
    }
  }
}
