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

import static junit.framework.Assert.assertEquals;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.sonar.api.utils.SonarException;

/**
 * @author Matthijs Galesloot
 */
public class XmlSchemaCheckTest extends AbstractCheckTester {


  @Test(expected = SonarException.class)
  public void missing_schema() throws FileNotFoundException {
    parseAndCheck(AANKONDIGINGEN_FILE, createCheck("does-not-exist", null));
  }

  @Test
  public void schema_as_external_path() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(AANKONDIGINGEN_FILE, createCheck("src/main/resources/org/sonar/plugins/xml/schemas/xhtml1/xhtml1-frameset.xsd", null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 162, sourceCode.getXmlIssues().size());
  }

  @Test
  public void test_file_pattern() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(CATALOG_FILE, createCheck("src/test/resources/checks/generic/catalog.xsd", "**/generic/**.xml"));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  @Test
  public void validate_maven_pom() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(POM_FILE, createCheck(XmlSchemaCheck.DEFAULT_SCHEMA, null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violate_auto_detect_check() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(AANKONDIGINGEN_FILE, createCheck(XmlSchemaCheck.DEFAULT_SCHEMA, null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 164, sourceCode.getXmlIssues().size());
  }

  /**
   * SONARXML-13
   */
  @Test
  public void no_issue_on_corrupted_file() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(WRONG_AMPERSAND_FILE, createCheck(XmlSchemaCheck.DEFAULT_SCHEMA, null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violate_builtin_xhtml_schema_check() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(SALES_ORDER_FILE, createCheck("xhtml1-transitional", null));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 2, sourceCode.getXmlIssues().size());
    assertEquals(16, sourceCode.getXmlIssues().get(0).getLine());
  }

  @Test
  public void violate_facelets_schema() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(SALES_ORDER2_FILE, createCheck("http://java.sun.com/jsf/core", null));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violate_jsf_schema() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(SALES_ORDER2_FILE, createCheck("http://java.sun.com/jsf/html", null));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertEquals(8, sourceCode.getXmlIssues().get(0).getLine());
  }

  @Test
  public void violate_local_xml_schema_check() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(CATALOG_FILE, createCheck("src/test/resources/checks/generic/catalog.xsd", null));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertEquals(5, sourceCode.getXmlIssues().get(0).getLine());
  }

  @Test
  public void violate_strict_html1_check() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(AANKONDIGINGEN_FILE, createCheck("xhtml1-strict", null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 164, sourceCode.getXmlIssues().size());
  }

  private static XmlSchemaCheck createCheck(String schema, String filePattern) {
    XmlSchemaCheck check = new XmlSchemaCheck();
    check.setSchemas(schema);
    check.setFilePattern(filePattern);

    return check;
  }
}
