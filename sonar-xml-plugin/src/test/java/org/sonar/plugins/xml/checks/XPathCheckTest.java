/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
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

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Matthijs Galesloot
 */
public class XPathCheckTest extends AbstractCheckTester {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Test
  public void violateXPathCheck() throws IOException {
    File tempFile = testFolder.newFile("file.xml");

    FileUtils.write(tempFile, "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
      + "xmlns:ui=\"http://java.sun.com/jsf/facelets\">"
      + "<body><br /></body></html>");

    XmlSourceCode sourceCode = parseAndCheck(tempFile, createCheck("//br"));

    assertEquals("Incorrect number of violations", 1, sourceCode.getXmlIssues().size());
    assertEquals(1, sourceCode.getXmlIssues().get(0).getLine());
  }

  @Test
  public void violateXPathWithNamespacesCheck() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(SALES_ORDER_FILE, createCheck("//ui:define[@name='title']"));

    assertEquals("Incorrect number of violations", 1, sourceCode.getXmlIssues().size());
    assertEquals(26, sourceCode.getXmlIssues().get(0).getLine());
  }

  /**
   * SONARXML-19
   */
  @Test
  public void report_issue_on_correct_line_for_file_with_char_before_prolog() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(CHAR_BEFORE_ROLOG_FILE, createCheck("//dependency/version"));

    assertEquals("Incorrect number of violations", 1, sourceCode.getXmlIssues().size());
    assertEquals(18, sourceCode.getXmlIssues().get(0).getLine());
  }

  /**
   * SONARPLUGINS-1765
   */
  @Test
  public void xpathRuleShouldNotCreateViolationForInvalidDocument() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(SONARSOURCE_FILE, createCheck("//link[@rel]"));
    assertEquals("Incorrect number of violations", 0, sourceCode.getXmlIssues().size());
  }

  private static XPathCheck createCheck(String expression) {
    XPathCheck check = new XPathCheck();

    check.setExpression(expression);

    return check;
  }
}
