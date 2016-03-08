/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthijs Galesloot
 */
public class NewlineCheckTest extends AbstractCheckTester {

  @Test
  public void checkNewlines() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html>\n<body><br>hello</br></body>\n</html>"),
      new NewlineCheck());

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 2, sourceCode.getXmlIssues().size());
  }

  @Test
  public void checkCommentSameLineNoIssue() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<tag> <!-- comment: should not raise an issue --> \n" +
        "</tag>"),
      new NewlineCheck());

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void checkCommentContent() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<tag> <!-- comment: should not raise an issue --> </tag>"),
      new NewlineCheck());

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  @Test
  public void checkMultipleNewlines() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html>\n<body><br /><br>hello</br></body>\n</html>"),
        new NewlineCheck());

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 3, sourceCode.getXmlIssues().size());
  }

  @Test
  public void checkCommentsOK() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html>\n<body>\n<!-- hello -->\n</body>\n</html>"),
      new NewlineCheck());

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void checkCommentsNotOK() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html>\n<body>\n<!-- hello --></body>\n</html>"),
      new NewlineCheck());

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void checkClosedTag() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html>\n<body/>\n</html>"),
      new NewlineCheck());

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

}
