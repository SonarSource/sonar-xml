/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
