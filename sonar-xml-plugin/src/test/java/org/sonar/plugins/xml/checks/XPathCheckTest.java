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

import java.io.FileNotFoundException;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Matthijs Galesloot
 */
public class XPathCheckTest extends AbstractCheckTester {

  @Test
  public void violateXPathCheck() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html xmlns=\"http://www.w3.org/1999/xhtml\" "
        + "xmlns:ui=\"http://java.sun.com/jsf/facelets\">"
        + "<body><br /></body></html>"),
      createCheck("//br"));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertThat(sourceCode.getXmlIssues().get(0).getLine()).isEqualTo(1);
  }

  @Test
  public void violateXPathOnFile() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html xmlns=\"http://www.w3.org/1999/xhtml\" "
        + "xmlns:ui=\"http://java.sun.com/jsf/facelets\">"
        + "<body><br /></body></html>"),
      createCheck("count(//br)>0"));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertNull(sourceCode.getXmlIssues().get(0).getLine());
  }

  @Test
  public void noIssueOnFile() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html xmlns=\"http://www.w3.org/1999/xhtml\" "
        + "xmlns:ui=\"http://java.sun.com/jsf/facelets\">"
        + "<body></body></html>"),
      createCheck("count(//br)>0"));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violateXPathWithNamespacesCheck() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(SALES_ORDER_FILE, createCheck("//ui:define[@name='title']"));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertThat(sourceCode.getXmlIssues().get(0).getLine()).isEqualTo(26);
  }

  /**
   * SONARXML-19
   */
  @Test
  public void report_issue_on_correct_line_for_file_with_char_before_prolog() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(CHAR_BEFORE_ROLOG_FILE, createCheck("//dependency/version"));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertThat(sourceCode.getXmlIssues().get(0).getLine()).isEqualTo(18);
  }

  /**
   * SONARPLUGINS-1765
   */
  @Test
  public void xpathRuleShouldNotCreateViolationForInvalidDocument() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(SONARSOURCE_FILE, createCheck("//link[@rel]"));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  private static XPathCheck createCheck(String expression) {
    XPathCheck check = new XPathCheck();

    check.setExpression(expression);

    return check;
  }

}
