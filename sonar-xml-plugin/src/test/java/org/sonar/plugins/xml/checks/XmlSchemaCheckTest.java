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

import org.jfree.util.Log;
import org.junit.Test;
import org.sonar.api.utils.SonarException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static junit.framework.Assert.assertEquals;

/**
 * @author Matthijs Galesloot
 */
public class XmlSchemaCheckTest extends AbstractCheckTester {

  private static final String SRC_TEST_RESOURCES_CHECKS_GENERIC_TENDER_NED_AANKONDIGINGEN_XHTML = "src/test/resources/checks/generic/TenderNed - Aankondigingen.xhtml";
  private static final String SCHEMAS = "schemas";
  private static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  @Test(expected = SonarException.class)
  public void missingSchema() throws FileNotFoundException {
    String fileName = SRC_TEST_RESOURCES_CHECKS_GENERIC_TENDER_NED_AANKONDIGINGEN_XHTML;
    FileReader reader = new FileReader(fileName);
    parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "does-not-exist");
  }

  @Test
  public void schemaAsExternalPath() throws FileNotFoundException {
    String fileName = SRC_TEST_RESOURCES_CHECKS_GENERIC_TENDER_NED_AANKONDIGINGEN_XHTML;
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS,
        "src/main/resources/org/sonar/plugins/xml/schemas/xhtml1/xhtml1-frameset.xsd");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 162, sourceCode.getXmlIssues().size());
  }

  @Test
  public void testFilePattern() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/catalog.xml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, "filePattern", "**/generic/**.xml",
        SCHEMAS, "src/test/resources/checks/generic/catalog.xsd");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  @Test
  public void validateMavenPom() throws FileNotFoundException {
    String fileName = "pom.xml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "autodetect");

    if (sourceCode.getXmlIssues().size() > 0) {
      Log.error(sourceCode.getXmlIssues().get(0).getMessage());
    }
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violateAutoDetectCheck() throws FileNotFoundException {
    String fileName = SRC_TEST_RESOURCES_CHECKS_GENERIC_TENDER_NED_AANKONDIGINGEN_XHTML;
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "autodetect");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 164, sourceCode.getXmlIssues().size());
  }

  /**
   * SONARXML-13
   */
  @Test
  public void no_issue_on_corrupted_file() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/wrong-ampersand.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "autodetect");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violateBuiltinXhtmlSchemaCheck() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "xhtml1-transitional");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 2, sourceCode.getXmlIssues().size());
    assertEquals(16, sourceCode.getXmlIssues().get(0).getLine());
  }

  @Test
  public void violateFaceletsSchema() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder2.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS,
        "http://java.sun.com/jsf/core");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
    // assertEquals((Integer) , sourceCode.getIssues().get(0).getLineId());
  }

  @Test
  public void violateJsfSchema() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder2.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS,
        "http://java.sun.com/jsf/html");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertEquals(8, sourceCode.getXmlIssues().get(0).getLine());
  }

  @Test
  public void violateLocalXmlSchemaCheck() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/catalog.xml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS,
        "src/test/resources/checks/generic/catalog.xsd");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertEquals(5, sourceCode.getXmlIssues().get(0).getLine());
  }

  @Test
  public void violateStrictHtml1heck() throws FileNotFoundException {
    String fileName = SRC_TEST_RESOURCES_CHECKS_GENERIC_TENDER_NED_AANKONDIGINGEN_XHTML;
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "xhtml1-strict");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 164, sourceCode.getXmlIssues().size());
  }
}
