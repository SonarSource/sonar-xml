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
  public void single_place() throws Exception {
    SonarXmlCheckVerifier.verifyIssues("tabsSinglePlace.xml", new TabCharacterCheck());
  }

  @Test
  public void tabs_on_three_lines() throws Exception {
    SonarXmlCheckVerifier.verifyIssues("tabsOnThreeLines.xml", new TabCharacterCheck());
  }

  @Test
  public void tabs_everywhere() throws Exception {
    SonarXmlCheckVerifier.verifyIssues("tabsEverywhere.xml", new TabCharacterCheck());
  }

  @Test
  public void tabs_max_reported_without_extra() throws Exception {
    SonarXmlCheckVerifier.verifyIssues("tabsMaxReportedWithoutExtra.xml", new TabCharacterCheck());
  }

  @Test
  public void no_tabs() throws Exception {
    SonarXmlCheckVerifier.verifyNoIssue("noTabs.xml", new TabCharacterCheck());
  }

}
