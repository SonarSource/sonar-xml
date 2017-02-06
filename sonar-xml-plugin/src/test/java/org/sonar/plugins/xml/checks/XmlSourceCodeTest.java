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
  public void unknown_file() throws Exception {
    File file = new File("src/test/resources/unknown.xml");
    DefaultInputFile inputFile = new DefaultInputFile(file.getPath()).setAbsolutePath(file.getAbsolutePath());
    DefaultFileSystem fileSystem = new DefaultFileSystem(new File(file.getParent()));
    XmlFile xmlFile = new XmlFile(inputFile, fileSystem);
    XmlSourceCode xmlSourceCode = new XmlSourceCode(xmlFile);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(file.getAbsolutePath());
    xmlSourceCode.parseSource();
  }

}
