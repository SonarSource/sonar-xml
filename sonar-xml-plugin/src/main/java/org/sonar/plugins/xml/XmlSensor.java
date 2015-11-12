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

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.component.Perspective;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Project;
import org.sonar.api.source.Highlightable;
import org.sonar.api.source.Highlightable.HighlightingBuilder;
import org.sonar.plugins.xml.checks.AbstractXmlCheck;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.checks.XmlFile;
import org.sonar.plugins.xml.checks.XmlIssue;
import org.sonar.plugins.xml.checks.XmlSourceCode;
import org.sonar.plugins.xml.highlighting.HighlightingData;
import org.sonar.plugins.xml.highlighting.XMLHighlighting;
import org.sonar.plugins.xml.language.Xml;

import javax.annotation.Nullable;
import java.util.List;

/**
 * XmlSensor provides analysis of xml files.
 *
 * @author Matthijs Galesloot
 */
public class XmlSensor implements Sensor {

  private final Checks<Object> checks;
  private final FileSystem fileSystem;
  private final ResourcePerspectives resourcePerspectives;
  private final FilePredicate mainFilesPredicate;
  private static final Logger LOG = LoggerFactory.getLogger(XmlSensor.class);
  private final FileLinesContextFactory fileLinesContextFactory;

  public XmlSensor(FileSystem fileSystem, ResourcePerspectives resourcePerspectives, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory) {
    this.fileLinesContextFactory = fileLinesContextFactory;
    this.checks = checkFactory.create(CheckRepository.REPOSITORY_KEY).addAnnotatedChecks(CheckRepository.getCheckClasses());
    this.fileSystem = fileSystem;
    this.resourcePerspectives = resourcePerspectives;
    this.mainFilesPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Xml.KEY));
  }

  /**
   * @inheritDoc
   */
  @Override
  public void analyse(Project project, SensorContext sensorContext) {
    for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {
      XmlFile xmlFile = new XmlFile(inputFile, fileSystem);

      computeLinesMeasures(sensorContext, xmlFile);
      runChecks(xmlFile);
    }
  }

  private void computeLinesMeasures(SensorContext context, XmlFile xmlFile) {
    LineCounter.analyse(context, fileLinesContextFactory, xmlFile, fileSystem.encoding());
  }

  private void runChecks(XmlFile xmlFile) {
    try {
      XmlSourceCode sourceCode = new XmlSourceCode(xmlFile);

      // Do not execute any XML rule when an XML file is corrupted (SONARXML-13)
      if (sourceCode.parseSource()) {
        for (Object check : checks.all()) {
          ((AbstractXmlCheck) check).setRuleKey(checks.ruleKey(check));
          ((AbstractXmlCheck) check).validate(sourceCode);
        }
        saveIssue(sourceCode);

        saveSyntaxHighlighting(new XMLHighlighting(xmlFile, fileSystem.encoding()).getHighlightingData(), xmlFile.getInputFile());
      }
    } catch (Exception e) {
      throw new IllegalStateException("Could not analyze the file " + xmlFile.getIOFile().getAbsolutePath(), e);
    }
  }

  private void saveSyntaxHighlighting(List<HighlightingData> highlightingDataList, InputFile inputFile) {
    Highlightable highlightable = perspective(Highlightable.class, inputFile);
    if (highlightable != null) {
      HighlightingBuilder highlightingBuilder = highlightable.newHighlighting();

      for (HighlightingData highlightingData : highlightingDataList) {
        highlightingBuilder.highlight(highlightingData.startOffset(), highlightingData.endOffset(), highlightingData.highlightCode());
      }

      highlightingBuilder.done();
    }

  }

  @Nullable
  <P extends Perspective<?>> P perspective(Class<P> clazz, InputFile file) {
    P result = resourcePerspectives.as(clazz, file);
    if (result == null) {
      LOG.warn("Could not get " + clazz.getCanonicalName() + " for " + file);
    }
    return result;
  }

  @VisibleForTesting
  protected void saveIssue(XmlSourceCode sourceCode) {
    for (XmlIssue xmlIssue : sourceCode.getXmlIssues()) {
      Issuable issuable = resourcePerspectives.as(Issuable.class, sourceCode.getInputFile());

      if (issuable != null) {
        issuable.addIssue(
          issuable.newIssueBuilder()
            .ruleKey(xmlIssue.getRuleKey())
            .line(xmlIssue.getLine())
            .message(xmlIssue.getMessage())
            .build());
      }
    }
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.hasFiles(mainFilesPredicate);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
