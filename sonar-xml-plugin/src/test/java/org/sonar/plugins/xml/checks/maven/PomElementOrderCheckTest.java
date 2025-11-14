/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks.maven;

import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class PomElementOrderCheckTest {

  @Test
  void should_raise_issue_if_order_is_wrong() {
    SonarXmlCheckVerifier.verifyIssues("Wrong1/pom.xml", new PomElementOrderCheck());
  }

  @Test
  void should_raise_issue_with_location_only_between_first_and_last_wrong() {
    SonarXmlCheckVerifier.verifyIssues("Wrong2/pom.xml", new PomElementOrderCheck());
  }

  @Test
  void should_not_raise_issue_if_order_is_correct() {
    SonarXmlCheckVerifier.verifyNoIssue("Ok/pom.xml", new PomElementOrderCheck());
  }

}
