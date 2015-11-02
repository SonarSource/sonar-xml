/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
