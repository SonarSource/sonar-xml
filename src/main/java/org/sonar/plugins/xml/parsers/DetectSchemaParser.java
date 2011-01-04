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

package org.sonar.plugins.xml.parsers;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.impl.Constants;
import org.sonar.api.utils.SonarException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Detect DTD or Schema
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
public final class DetectSchemaParser {

  private static class Handler extends DefaultHandler implements LexicalHandler {

    private String dtd;
    private String schema;

    public void comment(char[] arg0, int arg1, int arg2) throws SAXException {

    }

    public void endCDATA() throws SAXException {

    }

    public void endDTD() throws SAXException {

    }

    public void endEntity(String name) throws SAXException {

    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      // ignore
    }

    public void startCDATA() throws SAXException {

    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
      dtd = systemId;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      schema = attributes.getValue("xmlns");

      // we are done, make the parser stop
      throw new SAXException("done");
    }

    public void startEntity(String name) throws SAXException {

    }
  }

  private static final SAXParserFactory SAX_FACTORY;

  /**
   * Build the SAXParserFactory.
   */
  static {

    SAX_FACTORY = SAXParserFactory.newInstance();

    try {
      SAX_FACTORY.setValidating(false);
      SAX_FACTORY.setNamespaceAware(false);
      SAX_FACTORY.setFeature("http://xml.org/sax/features/validation", false);
      SAX_FACTORY.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      SAX_FACTORY.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      SAX_FACTORY.setFeature("http://xml.org/sax/features/external-general-entities", false);
      SAX_FACTORY.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    } catch (SAXException e) {
      throw new SonarException(e);
    } catch (ParserConfigurationException e) {
      throw new SonarException(e);
    }
  }

  public static SAXParser newSaxParser() {
    try {
      return SAX_FACTORY.newSAXParser();
    } catch (SAXException e) {
      throw new SonarException(e);
    } catch (ParserConfigurationException e) {
      throw new SonarException(e);
    }
  }

  public String findSchema(InputStream input) {
    Handler handler = new Handler();

    try {
      SAXParser parser = newSaxParser();
      XMLReader xmlReader = parser.getXMLReader();
      xmlReader.setFeature(Constants.XERCES_FEATURE_PREFIX + "continue-after-fatal-error", true);
      xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
      parser.parse(input, handler);
    } catch (IOException e) {
      throw new SonarException(e);
    } catch (SAXException e) {
      if (!"done".equals(e.getMessage())) {
        throw new SonarException(e);
      }
    } finally {
      IOUtils.closeQuietly(input);
    }

    return handler.dtd == null ? handler.schema : handler.dtd;
  }
}
