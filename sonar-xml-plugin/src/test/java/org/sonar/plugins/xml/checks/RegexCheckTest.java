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

import static junit.framework.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

/**
 * @author Julien Gaston
 */
public class RegexCheckTest extends AbstractCheckTester {

  @Test
  public void violateRegexCheck() {

    String fragment = ""
        + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<person>\n"
        + "<firstname>John</firstname>\n"
        + "<lastname>Doe</lastname>\n"
        + "<zipcode>90210</zipcode><city tz=\"auto\"/>\n"
        + "</person>\n"
        + "";

    String regex = "<\\w+\\s*.*/>";
    Reader reader = new StringReader(fragment);
    XmlSourceCode sourceCode = parseAndCheck(reader, null, fragment, RegexCheck.class, "regex", regex);

    assertEquals("Incorrect number of violations", 1, sourceCode.getXmlIssues().size());
    assertEquals("Incorrect line number", 5, sourceCode.getXmlIssues().get(0).getLine());
  }

}
