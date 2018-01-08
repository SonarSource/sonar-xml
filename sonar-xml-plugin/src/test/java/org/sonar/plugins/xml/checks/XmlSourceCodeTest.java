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

import java.io.File;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

import static org.sonar.plugins.xml.compat.CompatibilityHelper.wrap;

public class XmlSourceCodeTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void unknown_file() {
    File file = new File("src/test/resources/unknown.xml");
    XmlSourceCode xmlSourceCode = createXmlSourceCode(file);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(file.getAbsolutePath());
    xmlSourceCode.parseSource();
  }

  private XmlSourceCode createXmlSourceCode(File file) {
    File moduleBaseDir = file.getParentFile().getAbsoluteFile();
    DefaultInputFile inputFile = new DefaultInputFile("modulekey", file.getName()).setModuleBaseDir(moduleBaseDir.toPath());
    DefaultFileSystem fileSystem = new DefaultFileSystem(file.getParentFile());
    XmlFile xmlFile = new XmlFile(wrap(inputFile), fileSystem);
    return new XmlSourceCode(xmlFile);
  }

}
