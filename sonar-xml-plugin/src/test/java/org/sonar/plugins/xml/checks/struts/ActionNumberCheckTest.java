/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks.struts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class ActionNumberCheckTest {

  private ActionNumberCheck check;

  @BeforeEach
  void setup() {
    check = new ActionNumberCheck();
  }

  @Test
  void struts_config_with_too_many_forwards() {
    SonarXmlCheckVerifier.verifyIssues("tooManyActionsDefault/struts-config.xml", check);
  }

  @Test
  void struts_config_with_too_many_forwards_custom() {
    check.maximumForwards = 3;
    SonarXmlCheckVerifier.verifyIssues("tooManyActionsCustom/struts-config.xml", check);
  }

  @Test
  void not_a_struts_config_xml() {
    SonarXmlCheckVerifier.verifyNoIssue("../irrelevant.xml", check);
  }
}
