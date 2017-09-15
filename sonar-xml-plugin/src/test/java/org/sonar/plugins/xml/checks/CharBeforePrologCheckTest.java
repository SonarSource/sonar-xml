/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;

import static org.junit.Assert.assertEquals;

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
