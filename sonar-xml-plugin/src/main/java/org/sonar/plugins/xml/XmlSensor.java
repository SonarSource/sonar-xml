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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xml.checks.AbstractXmlCheck;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.checks.XmlIssue;
import org.sonar.plugins.xml.checks.XmlSourceCode;
import org.sonar.plugins.xml.language.Xml;

import com.google.common.annotations.VisibleForTesting;

/**
 * XmlSensor provides analysis of xml files.
 *
 * @author Matthijs Galesloot
 */
public class XmlSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(XmlSensor.class);

  private final Checks<Object> checks;
  private final FileSystem fileSystem;
  private final ResourcePerspectives resourcePerspectives;
  private final FilePredicate mainFilesPredicate;

  public XmlSensor(FileSystem fileSystem, ResourcePerspectives resourcePerspectives, CheckFactory checkFactory) {
    this.checks = checkFactory.create(CheckRepository.REPOSITORY_KEY).addAnnotatedChecks(CheckRepository.getCheckClasses());
    this.fileSystem = fileSystem;
    this.resourcePerspectives = resourcePerspectives;
    this.mainFilesPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Xml.KEY));
  }

  /**
   * Analyze the XML files.
   */
  public void analyse(Project project, SensorContext sensorContext) {
    for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {

      try {
        XmlSourceCode sourceCode = new XmlSourceCode(inputFile);

        // Do not execute any XML rule when an XML file is corrupted (SONARXML-13)
        if (sourceCode.parseSource(fileSystem)) {
          for (Object check : checks.all()) {
            ((AbstractXmlCheck) check).setRuleKey(checks.ruleKey(check));
            ((AbstractXmlCheck) check).validate(sourceCode);
          }
          saveIssue(sourceCode);
        }
      } catch (Exception e) {
        LOG.error("Could not analyze the file " + inputFile.file().getAbsolutePath(), e);
      }
    }
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

  /**
   * This sensor only executes on projects with active XML rules.
   */
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.hasFiles(mainFilesPredicate);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
