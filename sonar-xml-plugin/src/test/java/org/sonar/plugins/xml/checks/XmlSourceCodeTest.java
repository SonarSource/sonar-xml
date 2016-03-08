/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
