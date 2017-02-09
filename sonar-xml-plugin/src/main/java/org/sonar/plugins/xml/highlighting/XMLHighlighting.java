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
package org.sonar.plugins.xml.highlighting;

import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.plugins.xml.checks.XmlFile;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class XMLHighlighting {

  private static final String XML_DECLARATION_TAG = "<?xml";
  private final int delta;

  private List<HighlightingData> highlighting = new ArrayList<>();
  private String content;

  private int currentStartOffset = -1;
  private TypeOfText currentCode = null;

  private static final Logger LOG = LoggerFactory.getLogger(XMLHighlighting.class);

  public XMLHighlighting(XmlFile xmlFile, Charset charset) throws IOException {
    content = xmlFile.getContents();
    delta = xmlFile.getOffsetDelta();

    try (InputStream inputStream = xmlFile.getInputStream()) {
      highlightXML(new InputStreamReader(inputStream, charset));
    } catch (XMLStreamException e) {
      LOG.warn("Can't highlight following file : " + xmlFile.getAbsolutePath(), e);
    }
  }

  public XMLHighlighting(String xmlStrContent) {
    delta = 0;
    content = xmlStrContent;
    try {
      highlightXML(new StringReader(xmlStrContent));
    } catch (XMLStreamException e) {
      LOG.warn("Can't highlight following code : \n" + xmlStrContent, e);
    }
  }

  public List<HighlightingData> getHighlightingData() {
    return highlighting;
  }

  public void highlightXML(Reader reader) throws XMLStreamException {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, "false");
    factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, "false");
    XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
    highlightXmlDeclaration();

    while (xmlReader.hasNext()) {
      Location prevLocation = xmlReader.getLocation();
      xmlReader.next();
      int startOffset = xmlReader.getLocation().getCharacterOffset();
      closeHighlighting(startOffset);

      switch (xmlReader.getEventType()) {
        case XMLStreamConstants.START_ELEMENT:
          highlightStartElement(xmlReader, startOffset);
          break;

        case XMLStreamConstants.END_ELEMENT:
          highlightEndElement(xmlReader, prevLocation, startOffset);
          break;

        case XMLStreamConstants.CDATA:
          highlightCData(startOffset);
          break;

        case XMLStreamConstants.DTD:
          highlightDTD(startOffset);
          break;

        case XMLStreamConstants.COMMENT:
          addUnclosedHighlighting(startOffset, TypeOfText.STRUCTURED_COMMENT);
          break;

        default:
          break;
      }
    }
  }

  private void highlightDTD( int startOffset) {
    int closingBracketStartOffset;
    closingBracketStartOffset = getTagClosingBracketStartOffset(startOffset);
    addHighlighting(startOffset, startOffset + 9, TypeOfText.STRUCTURED_COMMENT);
    addHighlighting(closingBracketStartOffset, closingBracketStartOffset + 1, TypeOfText.STRUCTURED_COMMENT);
  }

  private void highlightCData(int startOffset) {
    if (!content.substring(startOffset).startsWith("<![CDATA[")) {
      // Ignoring secondary CDATA event
      // See https://docs.oracle.com/javase/7/docs/api/javax/xml/stream/XMLStreamReader.html#next()
      return;
    }

    int closingBracketStartOffset = getCDATAClosingBracketStartOffset(startOffset);

    // 9 is length of "<![CDATA["
    addHighlighting(startOffset, startOffset + 9, TypeOfText.KEYWORD);

    // highlight "]]>"
    addHighlighting(closingBracketStartOffset - 2, closingBracketStartOffset + 1, TypeOfText.KEYWORD);
  }

  private void highlightEndElement(XMLStreamReader xmlReader, Location prevLocation, int startOffset) {
    int closingBracketStartOffset = getTagClosingBracketStartOffset(startOffset);

    boolean isEmptyElement = prevLocation.getLineNumber() == xmlReader.getLocation().getLineNumber()
      && prevLocation.getColumnNumber() == xmlReader.getLocation().getColumnNumber();

    if (isEmptyElement) {
      // empty (or autoclosing) element is raised twice as start and end element, so we need to highlight closing "/" which is placed just before ">"
      addHighlighting(closingBracketStartOffset - 1, closingBracketStartOffset, TypeOfText.KEYWORD);

    } else {
      addHighlighting(startOffset, closingBracketStartOffset + 1, TypeOfText.KEYWORD);
    }
  }

  private void highlightStartElement(XMLStreamReader xmlReader, int startOffset) {
    int closingBracketStartOffset = getTagClosingBracketStartOffset(startOffset);
    int endOffset = startOffset + getNameWithNamespaceLength(xmlReader) + 1;

    addHighlighting(startOffset, endOffset, TypeOfText.KEYWORD);
    highlightAttributes(endOffset, closingBracketStartOffset);
    addHighlighting(closingBracketStartOffset, closingBracketStartOffset + 1, TypeOfText.KEYWORD);
  }

  private void highlightXmlDeclaration() {
    int startOffset = content.startsWith(XmlFile.BOM_CHAR) ? 1 : 0;
    if (content.startsWith(XML_DECLARATION_TAG, startOffset)) {
      int closingBracketStartOffset = getTagClosingBracketStartOffset(startOffset);

      addHighlighting(startOffset, startOffset + XML_DECLARATION_TAG.length(), TypeOfText.KEYWORD);
      highlightAttributes(startOffset + XML_DECLARATION_TAG.length(), closingBracketStartOffset);
      addHighlighting(closingBracketStartOffset - 1, closingBracketStartOffset + 1, TypeOfText.KEYWORD);
    }
  }

  private void highlightAttributes(int from, int to) {
    int counter = from + 1;

    Integer startOffset = null;
    Character attributeValueQuote = null;

    while (counter < to) {
      char c = content.charAt(counter);

      if (startOffset == null && !Character.isWhitespace(c)) {
        startOffset = counter;
      }


      if (attributeValueQuote != null && attributeValueQuote == c) {
        addHighlighting(startOffset, counter + 1, TypeOfText.STRING);
        counter++;
        startOffset = null;
        attributeValueQuote = null;
      }

      if (c == '=' && attributeValueQuote == null) {
        addHighlighting(startOffset, counter, TypeOfText.CONSTANT);

        do {
          counter++;
          c = content.charAt(counter);
        } while (c != '\'' && c != '"');

        startOffset = counter;
        attributeValueQuote = c;
      }


      counter++;
    }
  }

  private int getTagClosingBracketStartOffset(int startOffset) {
    return getClosingBracketStartOffset(startOffset, false);
  }

  private int getCDATAClosingBracketStartOffset(int startOffset) {
    return getClosingBracketStartOffset(startOffset, true);
  }

  private int getClosingBracketStartOffset(int startOffset, boolean isCDATA) {
    int counter = startOffset + 1;
    while (counter < content.length()) {
      if (content.charAt(counter) == '>' && bracketsBefore(isCDATA, counter)) {
        return counter;
      }
      counter++;
    }

    throw new IllegalStateException("No \">\" found.");
  }

  private boolean bracketsBefore(boolean isCDATA, int counter) {
    return !isCDATA || (content.charAt(counter - 1) == ']' && content.charAt(counter - 2) == ']');
  }

  private static int getNameWithNamespaceLength(XMLStreamReader streamReader) {
    int prefixLength = 0;
    if (!streamReader.getName().getPrefix().isEmpty()) {
      prefixLength = streamReader.getName().getPrefix().length() + 1;
    }

    return prefixLength + streamReader.getLocalName().length();
  }

  private void addHighlighting(int startOffset, int endOffset, TypeOfText typeOfText) {
    highlighting.add(new HighlightingData(startOffset + delta, endOffset + delta, typeOfText));
  }

  private void addUnclosedHighlighting(int startOffset, TypeOfText code) {
    currentStartOffset = startOffset;
    currentCode = code;
  }

  private void closeHighlighting(int endOffset) {
    if (currentCode != null) {
      addHighlighting(currentStartOffset, endOffset, currentCode);
      currentCode = null;
    }
  }

}
