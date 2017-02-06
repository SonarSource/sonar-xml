/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
