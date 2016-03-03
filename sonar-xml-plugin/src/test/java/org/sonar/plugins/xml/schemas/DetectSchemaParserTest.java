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
