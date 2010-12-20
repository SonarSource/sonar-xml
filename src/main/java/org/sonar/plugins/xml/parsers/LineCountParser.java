/*
 * Sonar Xml Plugin
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

import org.apache.xerces.impl.Constants;
import org.sonar.api.utils.SonarException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Comment Counting in XML files
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
public final class LineCountParser {

  private class CommentHandler extends DefaultHandler implements LexicalHandler {

    private int currentCommentLine = -1;
    private Locator locator;
    private int numCommentLines;

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      checkComment();
    }

    private void checkComment() {
      if (currentCommentLine >= 0) {
        if (locator.getLineNumber() > currentCommentLine) {
          numCommentLines++;
          currentCommentLine = -1;
        }
      }
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
      for (int i = 0; i < length; i++) {
        if (ch[start + i] == '\n') {
          numCommentLines++;
        }
      }
      currentCommentLine = locator.getLineNumber();
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

    protected int getNumCommentLines() {
      return numCommentLines;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    public void startCDATA() throws SAXException {

    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      checkComment();
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

  public int countLinesOfComment(InputStream input) {
    SAXParser parser = newSaxParser();
    try {

      XMLReader xmlReader = parser.getXMLReader();
      xmlReader.setFeature(Constants.XERCES_FEATURE_PREFIX + "continue-after-fatal-error", true);
      CommentHandler commentHandler = new CommentHandler();
      xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", commentHandler);
      parser.parse(input, commentHandler);
      return commentHandler.getNumCommentLines();
    } catch (IOException e) {
      throw new SonarException(e);
    } catch (SAXException e) {
      throw new SonarException(e);
    }
  }

}
