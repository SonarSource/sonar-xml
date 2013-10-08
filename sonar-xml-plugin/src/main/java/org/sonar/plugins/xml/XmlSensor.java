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
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.xml.checks.AbstractXmlCheck;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.checks.XmlSourceCode;
import org.sonar.plugins.xml.language.Xml;

import java.util.Collection;

/**
 * XmlSensor provides analysis of xml files.
 *
 * @author Matthijs Galesloot
 */
public final class XmlSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(XmlSensor.class);

  private final Settings settings;
  private final AnnotationCheckFactory annotationCheckFactory;

  public XmlSensor(Settings settings, RulesProfile profile) {
    this.settings = settings;
    this.annotationCheckFactory = AnnotationCheckFactory.create(profile, CheckRepository.REPOSITORY_KEY, CheckRepository.getCheckClasses());
  }

  /**
   * Analyze the XML files.
   */
  public void analyse(Project project, SensorContext sensorContext) {
    Collection<AbstractXmlCheck> checks = annotationCheckFactory.getChecks();
    for (InputFile inputfile : XmlPlugin.getFiles(project, settings)) {

      try {
        File resource = XmlProjectFileSystem.fromIOFile(inputfile, project);

        XmlSourceCode sourceCode = new XmlSourceCode(resource, inputfile.getFile());

        for (AbstractXmlCheck check : checks) {
          check.setRule(annotationCheckFactory.getActiveRule(check).getRule());
          check.validate(sourceCode);
        }
        saveViolations(sensorContext, sourceCode);

      } catch (Exception e) {
        LOG.error("Could not analyze the file " + inputfile.getFile().getAbsolutePath(), e);
      }
    }
  }

  private void saveViolations(SensorContext sensorContext, XmlSourceCode sourceCode) {
    for (Violation violation : sourceCode.getViolations()) {
      sensorContext.saveViolation(violation);
    }
  }

  /**
   * This sensor only executes on projects with active XML rules.
   */
  public boolean shouldExecuteOnProject(Project project) {
    return Xml.KEY.equals(project.getLanguageKey());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
