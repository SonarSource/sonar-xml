/*
 * Sonar XML Plugin
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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.xml.checks.AbstractPageCheck;
import org.sonar.plugins.xml.checks.XmlSourceCode;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.rules.XmlMessagesMatcher;
import org.sonar.plugins.xml.rules.XmlRulesRepository;

import java.util.List;

/**
 * XmlSensor provides analysis of xml files.
 *
 * @author Matthijs Galesloot
 */
public final class XmlSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(XmlSensor.class);

  private final RulesProfile profile;
  private final RuleFinder ruleFinder;
  private final Settings settings;

  public XmlSensor(RulesProfile profile, RuleFinder ruleFinder, Settings settings) {
    this.profile = profile;
    this.ruleFinder = ruleFinder;
    this.settings = settings;
  }

  /**
   * Analyze the XML files.
   */
  public void analyse(Project project, SensorContext sensorContext) {

    List<AbstractPageCheck> checks = XmlRulesRepository.createChecks(profile, (String) project.getProperty(XmlPlugin.SCHEMAS));
    for (InputFile inputfile : XmlPlugin.getFiles(project, settings)) {

      try {
        File resource = XmlProjectFileSystem.fromIOFile(inputfile, project);

        XmlSourceCode sourceCode = new XmlSourceCode(resource, inputfile.getFile());

        for (AbstractPageCheck check : checks) {
          check.validate(sourceCode);
        }
        saveMetrics(sensorContext, sourceCode);

      } catch (Exception e) {
        LOG.error("Could not analyze the file " + inputfile.getFile().getAbsolutePath(), e);
      }
    }
  }

  private boolean hasActiveRules(RulesProfile profile) {
    for (ActiveRule activeRule : profile.getActiveRules()) {
      if (XmlRulesRepository.REPOSITORY_KEY.equals(activeRule.getRepositoryKey())) {
        return true;
      }
    }
    return false;
  }

  private void saveMetrics(SensorContext sensorContext, XmlSourceCode sourceCode) {

    XmlMessagesMatcher messagesMatcher = new XmlMessagesMatcher();

    for (Violation violation : sourceCode.getViolations()) {
      messagesMatcher.setRuleForViolation(ruleFinder, violation);
      sensorContext.saveViolation(violation);
    }
  }

  /**
   * This sensor only executes on projects with active XML rules.
   */
  public boolean shouldExecuteOnProject(Project project) {
    return StringUtils.equals(Xml.KEY, project.getLanguageKey()) && hasActiveRules(profile);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
