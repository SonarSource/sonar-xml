/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.xml.checks;

import org.junit.Test;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

public class TabCharacterCheckTest {

  @Test
  public void should_have_only_one_issue_without_secondaries() throws Exception {
    TabCharacterCheck check = new TabCharacterCheck();

    SonarXmlCheckVerifier.verifyIssueOnFile(
      "tabsEverywhere.xml",
      check,
      "Replace all tab characters in this file by sequences of white-spaces.");

    SonarXmlCheckVerifier.verifyIssueOnFile(
      "tabsSinglePlace.xml",
      check,
      "Replace all tab characters in this file by sequences of white-spaces.");
  }

  @Test
  public void should_have_only_one_issue_with_secondaries() throws Exception {
    TabCharacterCheck check = new TabCharacterCheck();
    check.setMarkAll(true);

    SonarXmlCheckVerifier.verifyIssueOnFile(
      "tabsEverywhere.xml",
      check,
      "Replace all tab characters in this file by sequences of white-spaces.",
      2, 3, 4);

    SonarXmlCheckVerifier.verifyIssueOnFile(
      "tabsSinglePlace.xml",
      check,
      "Replace all tab characters in this file by sequences of white-spaces.",
      4);
  }

  @Test
  public void should_have_only_no_issue() throws Exception {
    SonarXmlCheckVerifier.verifyNoIssue("noTabs.xml", new TabCharacterCheck());
  }

}
