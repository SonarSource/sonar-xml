/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
