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
