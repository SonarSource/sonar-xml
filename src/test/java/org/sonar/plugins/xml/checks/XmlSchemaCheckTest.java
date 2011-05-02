/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
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
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.jfree.util.Log;
import org.junit.Test;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.xml.SimpleRuleFinder;
import org.sonar.plugins.xml.rules.XmlMessagesMatcher;

/**
 * @author Matthijs Galesloot
 */
public class XmlSchemaCheckTest extends AbstractCheckTester {

  private static final String SRC_TEST_RESOURCES_CHECKS_GENERIC_TENDER_NED_AANKONDIGINGEN_XHTML = "src/test/resources/checks/generic/TenderNed - Aankondigingen.xhtml";
  private static final String SCHEMAS = "schemas";
  private static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  private void checkViolationMessages(XmlSourceCode sourceCode) {
    XmlMessagesMatcher messagesMatcher = new XmlMessagesMatcher();
    for (Violation v : sourceCode.getViolations()) {
      messagesMatcher.setRuleForViolation(new SimpleRuleFinder(createStandardRulesProfile()), v);
      assertFalse("Unresolved message: " + v.getMessage(), XmlSchemaCheck.class.getSimpleName().equals(v.getRule().getKey()));
    }
  }

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

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 162, sourceCode.getViolations().size());
  }

  @Test
  public void testFilePattern() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/catalog.xml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, "filePattern", "**/generic/**.xml",
        SCHEMAS, "src/test/resources/checks/generic/catalog.xsd");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getViolations().size());
  }

  @Test
  public void validateMavenPom() throws FileNotFoundException {
    String fileName = "pom.xml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS,
        "http://maven.apache.org/POM/4.0.0");

    if (sourceCode.getViolations().size() > 0) {
      Log.error(sourceCode.getViolations().get(0).getMessage());
    }
    checkViolationMessages(sourceCode);
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getViolations().size());
  }

  @Test
  public void violateAutoDetectCheck() throws FileNotFoundException {
    String fileName = SRC_TEST_RESOURCES_CHECKS_GENERIC_TENDER_NED_AANKONDIGINGEN_XHTML;
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "autodetect");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 164, sourceCode.getViolations().size());
  }
  
//  @Test(expected=NullPointerException.class)
//  public void failingParser() throws FileNotFoundException {
//    String fileName = "src/test/resources/checks/generic/header.html"; 
//    FileReader reader = new FileReader(fileName);
//    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "autodetect");
//
//    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 164, sourceCode.getViolations().size());
//  }

  @Test
  public void violateBuiltinXhtmlSchemaCheck() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "xhtml1-transitional");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 2, sourceCode.getViolations().size());
    assertEquals((Integer) 16, sourceCode.getViolations().get(0).getLineId());

    // check if all violations resolved to messages
    checkViolationMessages(sourceCode);
  }

  @Test
  public void violateFaceletsSchema() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder2.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS,
        "http://java.sun.com/jsf/core");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getViolations().size());
    // assertEquals((Integer) , sourceCode.getViolations().get(0).getLineId());
  }

  @Test
  public void violateJsfSchema() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder2.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS,
        "http://java.sun.com/jsf/html");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getViolations().size());
    assertEquals((Integer) 8, sourceCode.getViolations().get(0).getLineId());
  }

  @Test
  public void violateLocalXmlSchemaCheck() throws FileNotFoundException {

    String fileName = "src/test/resources/checks/generic/catalog.xml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS,
        "src/test/resources/checks/generic/catalog.xsd");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getViolations().size());
    assertEquals((Integer) 5, sourceCode.getViolations().get(0).getLineId());
  }

  @Test
  public void violateSonarSource() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/sonarsource.html";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "xhtml1-transitional");

    // check if all violations resolved to messages
    checkViolationMessages(sourceCode);
  }

  @Test
  public void violateStrictHtml1heck() throws FileNotFoundException {
    String fileName = SRC_TEST_RESOURCES_CHECKS_GENERIC_TENDER_NED_AANKONDIGINGEN_XHTML;
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null, XmlSchemaCheck.class, SCHEMAS, "xhtml1-strict");

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 164, sourceCode.getViolations().size());

    // check if all violations resolved to messages
    checkViolationMessages(sourceCode);
  }
}
