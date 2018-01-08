/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * @author Matthijs Galesloot
 */
public class IllegalTabCheckTest extends AbstractCheckTester {

  @Test
  public void checkIllegalTabMarkall() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html>\t\t\t<body>\t<br>hello</br></body>\n</html>"),
      createCheck(true));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 2, sourceCode.getXmlIssues().size());
  }

  @Test
  public void checkIllegalTabMarkone() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html>\t\t\t<body>\t<br>hello</br></body>\n</html>"),
      createCheck(false));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  private static IllegalTabCheck createCheck(boolean markAll) {
    IllegalTabCheck check = new IllegalTabCheck();

    check.setMarkAll(markAll);

    return check;
  }

}
