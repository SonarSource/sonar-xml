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

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

import static junit.framework.Assert.assertEquals;

/**
 * @author Matthijs Galesloot
 */
public class XPathCheckTest extends AbstractCheckTester {

  @Test
  public void violateXPathCheck() throws FileNotFoundException {

    String fragment = "<html xmlns=\"http://www.w3.org/1999/xhtml\" " + "xmlns:ui=\"http://java.sun.com/jsf/facelets\">"
      + "<body><br /></body></html>";

    Reader reader = new StringReader(fragment);
    XmlSourceCode sourceCode = parseAndCheck(reader, null, fragment, XPathCheck.class, "expression", "//br");

    assertEquals("Incorrect number of violations", 1, sourceCode.getViolations().size());
    assertEquals((Integer) 1, sourceCode.getViolations().get(0).getLineId());
  }

  @Test
  public void violateXPathWithNamespacesCheck() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new java.io.File(fileName), null, XPathCheck.class, "expression",
        "//ui:define[@name='title']");

    assertEquals("Incorrect number of violations", 1, sourceCode.getViolations().size());
    assertEquals((Integer) 26, sourceCode.getViolations().get(0).getLineId());
  }

  // SONARPLUGINS-1765
  @Test
  public void xpathRuleShouldNotCreateViolationForInvalidDocument() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/sonarsource.html";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new java.io.File(fileName), null, XPathCheck.class, "expression", "//link[@rel]");

    assertEquals("Incorrect number of violations", 0, sourceCode.getViolations().size());
  }

}
