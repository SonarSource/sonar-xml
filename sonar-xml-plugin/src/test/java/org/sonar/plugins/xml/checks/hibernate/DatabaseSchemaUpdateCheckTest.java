/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.plugins.xml.checks.hibernate;

import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class DatabaseSchemaUpdateCheckTest {

  private static final SonarXmlCheck CHECK = new DatabaseSchemaUpdateCheck();

  @Test
  void test() {
    SonarXmlCheckVerifier.verifyIssues("hibernate11.cfg.xml", CHECK);
    SonarXmlCheckVerifier.verifyIssues("hibernate20.cfg.xml", CHECK);
    SonarXmlCheckVerifier.verifyIssues("hibernate30.cfg.xml", CHECK);
  }

  @Test
  void no_issue() {
    SonarXmlCheckVerifier.verifyNoIssue("hibernate30NoProperty.cfg.xml", CHECK);
    SonarXmlCheckVerifier.verifyNoIssue("../irrelevant.xml", CHECK);
  }

}
