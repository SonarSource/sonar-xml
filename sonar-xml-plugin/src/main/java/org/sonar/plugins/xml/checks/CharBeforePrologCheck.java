/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
