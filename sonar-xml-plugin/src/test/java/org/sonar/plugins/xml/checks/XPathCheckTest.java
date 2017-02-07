/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.checks;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThat;

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
