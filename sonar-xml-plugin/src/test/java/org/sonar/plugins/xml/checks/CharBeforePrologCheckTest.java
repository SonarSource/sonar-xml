/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.checks;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;

/**
 * @author Matthijs Galesloot
 */
public class CharBeforePrologCheckTest extends AbstractCheckTester {

  @Test
  public void ko() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(CHAR_BEFORE_ROLOG_FILE, new CharBeforePrologCheck());
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  @Test
  public void ok() throws FileNotFoundException {
    XmlSourceCode sourceCode = parseAndCheck(POM_FILE, new CharBeforePrologCheck());
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());
  }

  @Test
  public void ok_with_bom() throws Exception {
    XmlSourceCode sourceCode = parseAndCheck(UTF8_BOM_FILE, new CharBeforePrologCheck());
    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 0, sourceCode.getXmlIssues().size());    
  }

  @Override
  protected DefaultFileSystem createFileSystem() {
    return super.createFileSystem().setEncoding(StandardCharsets.UTF_8);
  }

}
