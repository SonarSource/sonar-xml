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
package org.sonar.plugins.xml.checks;

import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class TabCharacterCheckTest {

  @Test
  void single_place() {
    SonarXmlCheckVerifier.verifyIssues("tabsSinglePlace.xml", new TabCharacterCheck());
  }

  @Test
  void tabs_on_three_lines() {
    SonarXmlCheckVerifier.verifyIssues("tabsOnThreeLines.xml", new TabCharacterCheck());
  }

  @Test
  void tabs_everywhere() {
    SonarXmlCheckVerifier.verifyIssues("tabsEverywhere.xml", new TabCharacterCheck());
  }

  @Test
  void tabs_max_reported_without_extra() {
    SonarXmlCheckVerifier.verifyIssues("tabsMaxReportedWithoutExtra.xml", new TabCharacterCheck());
  }

  @Test
  void no_tabs() {
    SonarXmlCheckVerifier.verifyNoIssue("noTabs.xml", new TabCharacterCheck());
  }

}
