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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Matthijs Galesloot
 */
public class XmlSchemaCheckTest extends AbstractCheckTester {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test(expected = IllegalStateException.class)
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
    assertThat(sourceCode.getXmlIssues().get(0).getLine()).isEqualTo(16);
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
    assertThat(sourceCode.getXmlIssues().get(0).getLine()).isEqualTo(8);
  }

  @Test
  public void violate_local_xml_schema_check() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(CATALOG_FILE, createCheck("src/test/resources/checks/generic/catalog.xsd", null));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
    assertThat(sourceCode.getXmlIssues().get(0).getLine()).isEqualTo(5);
  }

  @Test(expected = IllegalStateException.class)
  public void invalid_schema() throws Exception {
    parseAndCheck(CATALOG_FILE, createCheck("src/test/resources/checks/XmlSchemaCheck/invalid.xsd", null));
  }

  @Test
  public void schema_as_resource() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(CATALOG_FILE, createCheck("/org/sonar/plugins/xml/schemas/xml.xsd", null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  @Test
  public void schema_as_url() throws FileNotFoundException {
    String url = new File("src/test/resources/checks/generic/catalog.xsd").getAbsoluteFile().toURI().toString();
    XmlSourceCode sourceCode = parseAndCheck(CATALOG_FILE, createCheck(url, null));
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  @Test
  public void violate_strict_html1_check() throws FileNotFoundException {
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

  private static XmlSchemaCheck createCheck(String schema, String filePattern) {
    XmlSchemaCheck check = new XmlSchemaCheck();
    check.setSchemas(schema);
    check.setFilePattern(filePattern);

    return check;
  }
}
