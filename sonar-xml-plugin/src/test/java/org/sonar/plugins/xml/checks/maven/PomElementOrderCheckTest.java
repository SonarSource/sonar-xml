/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
