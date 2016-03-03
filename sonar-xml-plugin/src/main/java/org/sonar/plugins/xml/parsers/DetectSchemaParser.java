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
package org.sonar.plugins.xml.parsers;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.SAXParser;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.impl.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse the file quickly only to detect the sDTD or Schema.
 *
 * @author Matthijs Galesloot
 */
public final class DetectSchemaParser extends AbstractParser {

  /**
   * Doctype declaration in a Document.
   */
  public static class Doctype {

    private String dtd;
    private String namespace;

    public String getDtd() {
      return dtd;
    }

    public String getNamespace() {
      return namespace;
    }
  }

  /**
   * Exception to stop the parser from further processing.
   */
  private static class StopParserException extends SAXException {

    private static final long serialVersionUID = 1L;

  }

  private static class Handler extends DefaultHandler implements LexicalHandler {

    private Doctype doctype = new Doctype();

    @Override
    public void comment(char[] arg0, int arg1, int arg2) throws SAXException {
      // empty
    }

    @Override
    public void endCDATA() throws SAXException {
      // empty
    }

    @Override
    public void endDTD() throws SAXException {
      // empty
    }

    @Override
    public void endEntity(String name) throws SAXException {
      // empty
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      // ignore
    }

    @Override
    public void startCDATA() throws SAXException {
      // empty
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
      doctype.dtd = publicId;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      doctype.namespace = attributes.getValue("xmlns");

      // we are done, cause the parser to stop
      throw new StopParserException();
    }

    @Override
    public void startEntity(String name) throws SAXException {
      // empty
    }
  }

  /**
   * Find the Doctype (DTD or schema).
   */
  public Doctype findDoctype(InputStream input) {
    Handler handler = new Handler();

    try {
      SAXParser parser = newSaxParser(false);
      XMLReader xmlReader = parser.getXMLReader();
      xmlReader.setFeature(Constants.XERCES_FEATURE_PREFIX + "continue-after-fatal-error", true);
      xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
      parser.parse(input, handler);
      return handler.doctype;
    } catch (StopParserException e) {
      return handler.doctype;
    } catch (IOException | SAXException e) {
      throw new IllegalStateException(e);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }
}
