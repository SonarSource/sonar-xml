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
package org.sonar.plugins.xml.checks;

import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;

/**
 * Perform check for indenting of elements.
 *
 * @author Matthijs Galesloot
 */
@Rule(key = "S1778",
  name = "XML files containing a prolog header should start first with \"<?xml\" characters",
  priority = Priority.MAJOR)
@BelongsToProfile(title = CheckRepository.SONAR_WAY_PROFILE_NAME, priority = Priority.MAJOR)
public class CharBeforePrologCheck extends AbstractXmlCheck {

  @Override
  public void validate(XmlSourceCode xmlSourceCode) {
    setWebSourceCode(xmlSourceCode);

    if (getWebSourceCode().isPrologFirstInSource()) {
      createViolation(getWebSourceCode().getXMLPrologLine(), "Remove all character before \"<?xml\".");
    }
  }

}
