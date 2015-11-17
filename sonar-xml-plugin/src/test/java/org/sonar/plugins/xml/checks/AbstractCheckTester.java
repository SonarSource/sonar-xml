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

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.plugins.xml.AbstractXmlPluginTester;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class AbstractCheckTester extends AbstractXmlPluginTester {

  @org.junit.Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  protected static final File AANKONDIGINGEN_FILE = new File("src/test/resources/checks/generic/TenderNed-Aankondigingen.xhtml");
  protected static final File POM_FILE = new File("pom.xml");
  protected static final File CATALOG_FILE = new File("src/test/resources/checks/generic/catalog.xml");
  protected static final File WRONG_AMPERSAND_FILE = new File("src/test/resources/checks/generic/wrong-ampersand.xhtml");
  protected static final File SALES_ORDER_FILE = new File("src/test/resources/checks/generic/create-salesorder.xhtml");
  protected static final File SALES_ORDER2_FILE = new File("src/test/resources/checks/generic/create-salesorder2.xhtml");
  protected static final File CHAR_BEFORE_ROLOG_FILE = new File("src/test/resources/src/pom_with_chars_before_prolog.xml");
  protected static final File UTF8_BOM_FILE = new File("src/test/resources/checks/generic/utf8-bom.xml");
  protected static final File SONARSOURCE_FILE = new File("src/test/resources/checks/generic/sonarsource.html");

  protected static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  protected XmlSourceCode parseAndCheck(File file, AbstractXmlCheck check) {
    XmlSourceCode xmlSourceCode = new XmlSourceCode(new XmlFile(new DefaultInputFile(file.getPath()).setAbsolutePath(file.getAbsolutePath()), createFileSystem()));

    if (xmlSourceCode.parseSource()) {
      check.validate(xmlSourceCode);
    }

    return xmlSourceCode;
  }

  protected DefaultFileSystem createFileSystem() {
    File workDir = temporaryFolder.newFolder("temp");

    DefaultFileSystem fs = new DefaultFileSystem(workDir);
    fs.setEncoding(Charset.defaultCharset());
    fs.setWorkDir(workDir);

    return fs;
  }

  protected File createTempFile(String content) throws IOException {
    File f = temporaryFolder.newFile("file.xml");
    FileUtils.write(f, content);

    return f;
  }

}
