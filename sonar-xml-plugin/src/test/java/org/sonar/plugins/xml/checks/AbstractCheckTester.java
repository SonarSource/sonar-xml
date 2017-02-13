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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.plugins.xml.AbstractXmlPluginTester;
import org.sonar.plugins.xml.compat.CompatibleInputFile;

import static org.sonar.plugins.xml.compat.CompatibilityHelper.wrap;

public abstract class AbstractCheckTester extends AbstractXmlPluginTester {

  @org.junit.Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  static final File AANKONDIGINGEN_FILE = new File("src/test/resources/checks/generic/TenderNed-Aankondigingen.xhtml");
  static final File POM_FILE = new File("./pom.xml");
  static final File CATALOG_FILE = new File("src/test/resources/checks/generic/catalog.xml");
  static final File WRONG_AMPERSAND_FILE = new File("src/test/resources/checks/generic/wrong-ampersand.xhtml");
  static final File SALES_ORDER_FILE = new File("src/test/resources/checks/generic/create-salesorder.xhtml");
  static final File SALES_ORDER2_FILE = new File("src/test/resources/checks/generic/create-salesorder2.xhtml");
  static final File CHAR_BEFORE_ROLOG_FILE = new File("src/test/resources/src/pom_with_chars_before_prolog.xml");
  static final File UTF8_BOM_FILE = new File("src/test/resources/checks/generic/utf8-bom.xml");
  static final File UTF16_FILE = new File("src/test/resources/checks/generic/utf16.xml");
  static final File SONARSOURCE_FILE = new File("src/test/resources/checks/generic/sonarsource.html");

  static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  XmlSourceCode parseAndCheck(File file, AbstractXmlCheck check) {
    XmlSourceCode xmlSourceCode = new XmlSourceCode(new XmlFile(newInputFile(file), createFileSystem()));

    if (xmlSourceCode.parseSource()) {
      check.validate(xmlSourceCode);
    }

    return xmlSourceCode;
  }

  private CompatibleInputFile newInputFile(File file) {
    return wrap(new DefaultInputFile("modulekey", file.getName())
      .setModuleBaseDir(file.getParentFile().toPath())
      .setCharset(StandardCharsets.UTF_8));
  }

  protected DefaultFileSystem createFileSystem() {
    File workDir = temporaryFolder.newFolder("temp");

    DefaultFileSystem fs = new DefaultFileSystem(workDir);
    fs.setEncoding(Charset.defaultCharset());
    fs.setWorkDir(workDir);

    return fs;
  }

  File createTempFile(String content) throws IOException {
    File f = temporaryFolder.newFile("file.xml");
    FileUtils.write(f, content);

    return f;
  }

}
