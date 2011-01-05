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

import org.junit.Test;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.xml.SimpleRuleFinder;
import org.sonar.plugins.xml.rules.XmlMessagesMatcher;

/**
 * @author Matthijs Galesloot
 */
public class XmlSchemaCheckTest extends AbstractCheckTester {

  @Test
  public void testFilePattern() throws FileNotFoundException  {
    String fileName = "src/test/resources/checks/generic/catalog.xml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class,
        "filePattern", "**/generic/**.xml",
        "schemas", "src/test/resources/checks/generic/catalog.xsd");

    assertEquals("Incorrect number of violations", 1, sourceCode.getViolations().size());
  }

  @Test
  public void violateLocalXmlSchemaCheck() throws FileNotFoundException {

    String fileName = "src/test/resources/checks/generic/catalog.xml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "src/test/resources/checks/generic/catalog.xsd");

    assertEquals("Incorrect number of violations", 1, sourceCode.getViolations().size());
    assertEquals((Integer) 5, sourceCode.getViolations().get(0).getLineId());
  }

  @Test
  public void violateBuiltinXhtmlSchemaCheck() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "xhtml1-transitional" );

    assertEquals("Incorrect number of violations", 2, sourceCode.getViolations().size());
    assertEquals((Integer) 16, sourceCode.getViolations().get(0).getLineId());

    // check if all violations resolved to messages
    checkViolationMessages(sourceCode);
  }

  @Test
  public void violateSonarSource() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/sonarsource.html";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "xhtml1-transitional" );

    // check if all violations resolved to messages
    checkViolationMessages(sourceCode);
  }

  @Test
  public void violateStrictHtml1heck() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/TenderNed - Aankondigingen.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "xhtml1-strict" );

    assertEquals("Incorrect number of violations", 143, sourceCode.getViolations().size());

    // check if all violations resolved to messages
    checkViolationMessages(sourceCode);
  }

  @Test
  public void violateAutoDetectCheck() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/TenderNed - Aankondigingen.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "autodetect" );

    assertEquals("Incorrect number of violations", 143, sourceCode.getViolations().size());
  }

  @Test(expected=SonarException.class)
  public void missingSchema() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/TenderNed - Aankondigingen.xhtml";
    FileReader reader = new FileReader(fileName);
    parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "does-not-exist" );
  }

  @Test
  public void schemaAsExternalPath() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/TenderNed - Aankondigingen.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "src/main/resources/org/sonar/plugins/xml/schemas/xhtml1/xhtml1-frameset.xsd" );

    assertEquals("Incorrect number of violations", 141, sourceCode.getViolations().size());
  }

  private void checkViolationMessages(XmlSourceCode sourceCode) {
    XmlMessagesMatcher messagesMatcher = new XmlMessagesMatcher();
    for (Violation v : sourceCode.getViolations()) {
      messagesMatcher.setRuleForViolation(new SimpleRuleFinder(createStandardRulesProfile()), v);
      assertFalse("Unresolved message: "+ v.getMessage(), XmlSchemaCheck.class.getSimpleName().equals(v.getRule().getKey()));
    }
  }

  @Test
  public void violateJsfSchema() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder2.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "http://java.sun.com/jsf/html" );

    assertEquals("Incorrect number of violations", 1, sourceCode.getViolations().size());
    assertEquals((Integer) 8, sourceCode.getViolations().get(0).getLineId());
  }

  @Test
  public void violateFaceletsSchema() throws FileNotFoundException {
    String fileName = "src/test/resources/checks/generic/create-salesorder2.xhtml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "http://java.sun.com/jsf/core" );

    assertEquals("Incorrect number of violations", 0, sourceCode.getViolations().size());
    //assertEquals((Integer) , sourceCode.getViolations().get(0).getLineId());
  }

  @Test
  public void validateMavenPom() throws FileNotFoundException {
    String fileName = "pom.xml";
    FileReader reader = new FileReader(fileName);
    XmlSourceCode sourceCode = parseAndCheck(reader, new File(fileName), null,
        XmlSchemaCheck.class, "schemas", "http://maven.apache.org/POM/4.0.0" );

    assertEquals("Incorrect number of violations", 0, sourceCode.getViolations().size());
  }
}
