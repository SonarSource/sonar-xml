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

import java.io.IOException;

import org.junit.Test;

/**
 * @author Matthijs Galesloot
 */
public class IllegalTabCheckTest extends AbstractCheckTester {

  @Test
  public void checkIllegalTabMarkall() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html>\t\t\t<body>\t<br>hello</br></body>\n</html>"),
      createCheck(true));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 2, sourceCode.getXmlIssues().size());
  }

  @Test
  public void checkIllegalTabMarkone() throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile("<html>\t\t\t<body>\t<br>hello</br></body>\n</html>"),
      createCheck(false));

    assertEquals(INCORRECT_NUMBER_OF_VIOLATIONS, 1, sourceCode.getXmlIssues().size());
  }

  private static IllegalTabCheck createCheck(boolean markAll) {
    IllegalTabCheck check = new IllegalTabCheck();

    check.setMarkAll(markAll);

    return check;
  }

}
