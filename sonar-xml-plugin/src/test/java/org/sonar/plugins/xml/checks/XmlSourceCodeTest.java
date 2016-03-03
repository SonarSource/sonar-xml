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
