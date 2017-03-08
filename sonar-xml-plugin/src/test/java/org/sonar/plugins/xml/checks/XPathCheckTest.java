/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
import org.sonar.plugins.xml.parsers.ParseException;
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
  @Test(expected = ParseException.class)
  public void xpathRuleShouldRaiseExceptionOnInvalidDocument() throws FileNotFoundException {
    parseAndCheck(SONARSOURCE_FILE, createCheck("//link[@rel]"));
  }

  private static XPathCheck createCheck(String expression) {
    XPathCheck check = new XPathCheck();

    check.setExpression(expression);

    return check;
  }

}
