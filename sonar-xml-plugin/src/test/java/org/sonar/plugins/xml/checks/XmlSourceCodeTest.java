/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.checks;

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

public class XmlSourceCodeTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void unknown_file() {
    File file = new File("src/test/resources/unknown.xml");
    File moduleBaseDir = file.getParentFile().getAbsoluteFile();
    DefaultInputFile inputFile = new DefaultInputFile("modulekey", file.getName()).setModuleBaseDir(moduleBaseDir.toPath());
    DefaultFileSystem fileSystem = new DefaultFileSystem(file.getParentFile());
    XmlFile xmlFile = new XmlFile(inputFile, fileSystem);
    XmlSourceCode xmlSourceCode = new XmlSourceCode(xmlFile);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(file.getAbsolutePath());
    xmlSourceCode.parseSource();
  }

}
