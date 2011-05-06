/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
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

import java.io.FileNotFoundException;

import org.junit.Test;

/**
 * @author Matthijs Galesloot
 */
public class IllegalTabCheckTest extends AbstractCheckTester {

  @Test
  public void checkIllegalTabMarkall() throws FileNotFoundException {
    String fragment = "<html>\t\t\t<body>\t<br>hello</br></body>\n</html>";

    parseCheckAndAssert(fragment, IllegalTabCheck.class, 2, "markAll", "true");
  }
  
  @Test
  public void checkIllegalTabMarkone() throws FileNotFoundException {
    String fragment = "<html>\t\t\t<body>\t<br>hello</br></body>\n</html>";

    parseCheckAndAssert(fragment, IllegalTabCheck.class, 1, "markAll", "false");
  }
  
}
