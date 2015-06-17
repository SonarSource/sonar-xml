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

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.plugins.xml.AbstractXmlPluginTester;

public abstract class AbstractCheckTester extends AbstractXmlPluginTester {

  @org.junit.Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  protected static final java.io.File AANKONDIGINGEN_FILE = new java.io.File("src/test/resources/checks/generic/TenderNed-Aankondigingen.xhtml");
  protected static final java.io.File POM_FILE = new java.io.File("pom.xml");
  protected static final java.io.File CATALOG_FILE = new java.io.File("src/test/resources/checks/generic/catalog.xml");
  protected static final java.io.File WRONG_AMPERSAND_FILE = new java.io.File("src/test/resources/checks/generic/wrong-ampersand.xhtml");
  protected static final java.io.File SALES_ORDER_FILE = new java.io.File("src/test/resources/checks/generic/create-salesorder.xhtml");
  protected static final java.io.File SALES_ORDER2_FILE = new java.io.File("src/test/resources/checks/generic/create-salesorder2.xhtml");
  protected static final java.io.File CHAR_BEFORE_ROLOG_FILE = new java.io.File("src/test/resources/src/pom_with_chars_before_prolog.xml");
  protected static final java.io.File SONARSOURCE_FILE = new java.io.File("src/test/resources/checks/generic/sonarsource.html");

  protected static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  protected XmlSourceCode parseAndCheck(java.io.File file, AbstractXmlCheck check) {
    XmlSourceCode xmlSourceCode = new XmlSourceCode(new DefaultInputFile(file.getPath()).setAbsolutePath(file.getAbsolutePath()));

    if (xmlSourceCode.parseSource(createFileSystem())) {
      check.validate(xmlSourceCode);
    }

    return xmlSourceCode;
  }

  protected void parseCheckAndAssert(String fragment, Class<? extends AbstractXmlCheck> clazz, int numViolations, String... params) {
    Reader reader = new StringReader(fragment);
    // XmlSourceCode sourceCode = parseAndCheck(reader, null, fragment, clazz, params);

    // assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, numViolations, sourceCode.getXmlIssues().size());
  }

  protected DefaultFileSystem createFileSystem() {
    java.io.File workDir = temporaryFolder.newFolder("temp");

    DefaultFileSystem fs = new DefaultFileSystem(workDir);
    fs.setEncoding(Charset.defaultCharset());
    fs.setWorkDir(workDir);

    return fs;
  }

}
