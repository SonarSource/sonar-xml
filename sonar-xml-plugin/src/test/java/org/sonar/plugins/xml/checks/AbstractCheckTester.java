/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.checks;

import java.nio.charset.StandardCharsets;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.plugins.xml.AbstractXmlPluginTester;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

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
  static final File SONARSOURCE_FILE = new File("src/test/resources/checks/generic/sonarsource.html");

  static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  XmlSourceCode parseAndCheck(File file, AbstractXmlCheck check) {
    XmlSourceCode xmlSourceCode = new XmlSourceCode(new XmlFile(newInputFile(file), createFileSystem()));

    if (xmlSourceCode.parseSource()) {
      check.validate(xmlSourceCode);
    }

    return xmlSourceCode;
  }

  private InputFile newInputFile(File file) {
    return new DefaultInputFile("modulekey", file.getName())
      .setModuleBaseDir(file.getParentFile().toPath())
      .setCharset(StandardCharsets.UTF_8);
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
