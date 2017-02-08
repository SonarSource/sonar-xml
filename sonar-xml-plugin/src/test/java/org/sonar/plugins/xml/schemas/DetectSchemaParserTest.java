/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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
package org.sonar.plugins.xml.schemas;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsException;
import org.mockito.stubbing.Answer;
import org.sonar.plugins.xml.parsers.DetectSchemaParser;
import org.sonar.plugins.xml.parsers.DetectSchemaParser.Doctype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DetectSchemaParserTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  
  private static String fileName = "src/test/resources/checks/generic/create-salesorder.xhtml";

  private DetectSchemaParser detectSchemaParser = new DetectSchemaParser();

  @Test
  public void testDetectDTD() throws IOException {
    Doctype doctype = detectSchemaParser.findDoctype(FileUtils.openInputStream(new File(fileName)));
    assertNotNull(doctype.getDtd());
    InputStream input = SchemaResolver.getBuiltinSchema(doctype.getDtd());
    assertNotNull(input);
  }

  @Test
  public void testDetectSchema() throws IOException {
    String fragment = "<html xmlns=\"http://www.w3.org/1999/xhtml\"" + "xmlns:c=\"http://java.sun.com/jstl/core\" ></html>";
    Doctype doctype = detectSchemaParser.findDoctype(new ByteArrayInputStream(fragment.getBytes()));
    assertNotNull(doctype.getNamespace());
    assertEquals("http://www.w3.org/1999/xhtml", doctype.getNamespace());
  }

  @Test
  public void io_exception() throws Exception {
    Answer<Object> defaultAnswer = new ThrowsException(new IOException("myexception"));
    InputStream inputStream = Mockito.mock(InputStream.class, defaultAnswer);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("myexception");
    detectSchemaParser.findDoctype(inputStream);
  }
}
