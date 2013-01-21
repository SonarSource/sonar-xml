/*
 * Sonar XML Plugin
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
package org.sonar.plugins.xml.schemas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sonar.plugins.xml.parsers.DetectSchemaParser;
import org.sonar.plugins.xml.parsers.DetectSchemaParser.Doctype;

public class DetectSchemaParserTest {

  private static String fileName = "src/test/resources/checks/generic/create-salesorder.xhtml";

  @Test
  public void testDetectDTD() throws IOException {
    DetectSchemaParser detectSchemaParser = new DetectSchemaParser();
    Doctype doctype = detectSchemaParser.findDoctype(FileUtils.openInputStream(new File(fileName)));
    assertNotNull(doctype.getDtd());
    InputStream input = SchemaResolver.getBuiltinSchema(doctype.getDtd());
    assertNotNull(input);
  }

  @Test
  public void testDetectSchema() throws IOException {
    String fragment = "<html xmlns=\"http://www.w3.org/1999/xhtml\"" + "xmlns:c=\"http://java.sun.com/jstl/core\" ></html>";

    DetectSchemaParser detectSchemaParser = new DetectSchemaParser();
    Doctype doctype = detectSchemaParser.findDoctype(new ByteArrayInputStream(fragment.getBytes()));
    assertNotNull(doctype.getNamespace());
    assertEquals("http://www.w3.org/1999/xhtml", doctype.getNamespace());
  }
}
