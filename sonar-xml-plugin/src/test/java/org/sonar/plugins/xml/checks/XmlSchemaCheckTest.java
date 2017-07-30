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

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.plugins.xml.parsers.ParseException;
import org.sonar.api.utils.log.LogTester;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Matthijs Galesloot
 */
public class XmlSchemaCheckTest extends AbstractCheckTester {

  @Rule
  public LogTester logTester = new LogTester();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void missing_schema() {
    parseAndCheck(AANKONDIGINGEN_FILE, createCheck("does-not-exist", null));

    assertLog("Cannot validate file .*xhtml", true);
    assertLog("Cause: .*SchemaNotFoundException: Could not load schema \"does-not-exist\"", true);
  }

  @Test
  public void schema_as_external_path() {
    XmlSourceCode sourceCode = parseAndCheck(AANKONDIGINGEN_FILE, createCheck("src/main/resources/org/sonar/plugins/xml/schemas/xhtml1/xhtml1-frameset.xsd", null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 162, sourceCode.getXmlIssues().size());
  }

  @Test
  public void test_file_pattern() {
    XmlSourceCode sourceCode = parseAndCheck(CATALOG_FILE, createCheck("src/test/resources/checks/generic/catalog.xsd", "**/generic/**.xml"));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  @Test
  public void validate_maven_pom() {
    XmlSourceCode sourceCode = parseAndCheck(POM_FILE, createCheck(XmlSchemaCheck.DEFAULT_SCHEMA, null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violate_auto_detect_check() {
    XmlSourceCode sourceCode = parseAndCheck(AANKONDIGINGEN_FILE, createCheck(XmlSchemaCheck.DEFAULT_SCHEMA, null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 164, sourceCode.getXmlIssues().size());
  }

  /**
   * SONARXML-13
   */
  @Test(expected = ParseException.class)
  public void should_raise_exception_on_corrupted_file() throws FileNotFoundException {
    parseAndCheck(WRONG_AMPERSAND_FILE, createCheck(XmlSchemaCheck.DEFAULT_SCHEMA, null));
  }

  @Test
  public void violate_builtin_xhtml_schema_check() {
    XmlSourceCode sourceCode = parseAndCheck(SALES_ORDER_FILE, createCheck("xhtml1-transitional", null));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 2, sourceCode.getXmlIssues().size());
    assertThat(sourceCode.getXmlIssues().get(0).getLine()).isEqualTo(16);
  }

  @Test
  public void violate_facelets_schema() {
    XmlSourceCode sourceCode = parseAndCheck(SALES_ORDER2_FILE, createCheck("http://java.sun.com/jsf/core", null));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violate_jsf_schema() {
    XmlSourceCode sourceCode = parseAndCheck(SALES_ORDER2_FILE, createCheck("http://java.sun.com/jsf/html", null));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertThat(sourceCode.getXmlIssues().get(0).getLine()).isEqualTo(8);
  }

  @Test
  public void violate_local_xml_schema_check() {
    XmlSourceCode sourceCode = parseAndCheck(CATALOG_FILE, createCheck("src/test/resources/checks/generic/catalog.xsd", null));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertThat(sourceCode.getXmlIssues().get(0).getLine()).isEqualTo(5);
  }

  @Test
  public void invalid_schema() throws Exception {
    parseAndCheck(CATALOG_FILE, createCheck("src/test/resources/checks/XmlSchemaCheck/invalid.xsd", null));

    assertLog("Could not validate file .*catalog\\.xml.*", true);
    assertLog("Reason: Unable to create schema for [src/test/resources/checks/XmlSchemaCheck/invalid.xsd]", false);
    assertLog("Cause:  org.xml.sax.SAXParseException; Premature end of file.", false);
  }

  @Test
  public void schema_as_resource() {
    XmlSourceCode sourceCode = parseAndCheck(CATALOG_FILE, createCheck("/org/sonar/plugins/xml/schemas/xml.xsd", null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  @Test
  public void schema_as_url() {
    String url = new File("src/test/resources/checks/generic/catalog.xsd").getAbsoluteFile().toURI().toString();
    XmlSourceCode sourceCode = parseAndCheck(CATALOG_FILE, createCheck(url, null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violate_strict_html1_check() {
    XmlSourceCode sourceCode = parseAndCheck(AANKONDIGINGEN_FILE, createCheck("xhtml1-strict", null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 164, sourceCode.getXmlIssues().size());
  }

  @Test(expected = IllegalStateException.class)
  public void unknown_dom_implementation() throws Exception {
    try {
      System.setProperty(DOMImplementationRegistry.PROPERTY, "unknown.class.name");
      parseAndCheck(SALES_ORDER_FILE, createCheck("xhtml1-transitional", null));
    } finally {
      System.clearProperty(DOMImplementationRegistry.PROPERTY);
    }
  }
  
  @Test
  public void invalid_sax_feature() throws Exception {
    FileInputStream inputStream = new FileInputStream("src/main/resources/org/sonar/plugins/xml/schemas/xml.xsd");
    Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(inputStream));
    Validator validator = schema.newValidator();
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("xxx");
    XmlSchemaCheck.setFeature(validator, "xxx", true);
  }

  @Test
  public void fail_due_to_mixture_of_dtd_and_xsd_with_schema_provided() {
    File xmlFile = new File("src/test/resources/checks/XmlSchemaCheck/dtd_and_xsd/entities.xml");
    String schema = "src/test/resources/checks/XmlSchemaCheck/dtd_and_xsd/entities.xsd";
    parseAndCheck(xmlFile, createCheck(schema, null));
    
    assertLog("Unable to validate file .*entities\\.xml.*", true);

    String expectedErrorOnLinux = "Cause: .*nested\\.xml.*No such file or directory.*";
    String expectedErrorOnWindows = "Cause: .*nested\\.xml.*The system cannot find the file specified.*";
    assertTrue(logsContain(expectedErrorOnLinux) || logsContain(expectedErrorOnWindows));
  }

  @Test
  public void fail_due_to_mixture_of_dtd_and_xsd_with_autodetect() {
    File xmlFile = new File("src/test/resources/checks/XmlSchemaCheck/dtd_and_xsd/entities.xml");
    parseAndCheck(xmlFile, createCheck(XmlSchemaCheck.DEFAULT_SCHEMA, null));

    assertLog("Cannot validate file .*entities\\.xml.*", true);
    assertLog("Cause: .*SchemaNotFoundException: Could not load schema \"http://redone/cbs/apiEntities\"", true);
  }

  // To be fixed in SONARXML-59.
  @Test
  public void fail_due_to_usage_of_default_namespace() {
    File xmlFile = new File("src/test/resources/checks/XmlSchemaCheck/using_default_namespace.xml");
    parseAndCheck(xmlFile, createCheck(XmlSchemaCheck.DEFAULT_SCHEMA, null));

    assertLog("Could not validate file .*using_default_namespace\\.xml.*", true);
    assertLog("Reason: Unable to create schema for [http://www.springframework.org/schema/beans]", false);
    assertLog("Cause:  org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 55; White spaces are required between publicId and systemId.", false);
  }

  // To be fixed in SONARXML-59 (probably same cause as above).
  @Test
  public void fail_on_validating_schema() {
    File xmlFile = new File("src/test/resources/checks/XmlSchemaCheck/dtd_and_xsd/entities.xsd");
    parseAndCheck(xmlFile, createCheck(XmlSchemaCheck.DEFAULT_SCHEMA, null));

    assertLog("Could not validate file .*entities\\.xsd", true);
    assertLog("Reason: Unable to create schema for [http://www.w3.org/2001/XMLSchema]", false);
    assertLog("Cause:  org.xml.sax.SAXParseException; lineNumber: 7; columnNumber: 18; s4s-elt-character: Non-whitespace characters "
            + "are not allowed in schema elements other than 'xs:appinfo' and 'xs:documentation'. Saw 'XML Schema'.", false);
  }

  private static XmlSchemaCheck createCheck(String schema, String filePattern) {
    XmlSchemaCheck check = new XmlSchemaCheck();
    check.setSchemas(schema);
    check.setFilePattern(filePattern);

    return check;
  }
  
  private void assertLog(String expected, boolean isRegexp) {
    if (isRegexp) {
      Condition<String> regexpMatches = new Condition<String>(log -> Pattern.compile(expected).matcher(log).matches(), "");
         assertThat(logTester.logs())
           .filteredOn(regexpMatches)
           .as("None of the lines in " + logTester.logs() + " matches regexp [" + expected + "], but one line was expected to match")
           .isNotEmpty();
     } else {
         assertThat(logTester.logs()).contains(expected);
     }
  }

  private boolean logsContain(String expectedLogStringRegex) {
    boolean patternFound = false;
    Matcher matcher = Pattern.compile(expectedLogStringRegex).matcher("");
    for (String currentLogLine : logTester.logs()) {
      matcher.reset(currentLogLine);
      if (matcher.matches()) {
        patternFound = true;
      }
    }
    return patternFound;
  }

}
