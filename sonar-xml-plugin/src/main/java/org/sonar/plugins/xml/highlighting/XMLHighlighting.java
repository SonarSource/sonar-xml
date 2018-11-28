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
package org.sonar.plugins.xml.highlighting;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.xml.checks.XmlFile;

public class XMLHighlighting {

  private static final Logger LOG = Loggers.get(XMLHighlighting.class);
  private static final String XML_DECLARATION_TAG = "<?xml";

  private List<HighlightingData> highlighting = new ArrayList<>();

  private XmlLocation currentStartLocation = null;
  private TypeOfText currentCode = null;

  private XmlLocation xmlFileStartLocation;
  private String content;

  public XMLHighlighting(XmlFile xmlFile) throws IOException {
    this(xmlFile.getInputFile().contents(), String.format("Can't highlight following file : %s", xmlFile.uri()));
  }

  public XMLHighlighting(String xmlStrContent) {
    this(xmlStrContent, String.format("Can't highlight following code : %n%s", xmlStrContent));
  }

  private XMLHighlighting(String xmlStrContent, String errorMessage) {
    if (xmlStrContent.startsWith(XmlFile.BOM_CHAR)) {
      // remove it immediately
      xmlStrContent = xmlStrContent.substring(1);
    }
    int realStartIndex = xmlStrContent.indexOf(XML_DECLARATION_TAG);
    try {
      if (realStartIndex == -1) {
        xmlFileStartLocation = new XmlLocation(xmlStrContent);
        content = xmlStrContent;
      } else {
        content = xmlStrContent.substring(realStartIndex);
        xmlFileStartLocation = new XmlLocation(xmlStrContent).moveBefore(XML_DECLARATION_TAG);
      }
      highlightXML();
    } catch (XMLStreamException e) {
      LOG.warn(errorMessage, e);
    }
  }

  public List<HighlightingData> getHighlightingData() {
    return highlighting;
  }

  public void highlightXML() throws XMLStreamException {
    Reader reader = new StringReader(content);

    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
    XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
    highlightXmlDeclaration();

    while (xmlReader.hasNext()) {
      XmlLocation prevLocation = new XmlLocation(content, xmlReader.getLocation());
      xmlReader.next();
      XmlLocation startLocation = new XmlLocation(content, xmlReader.getLocation());
      closeHighlighting(startLocation);

      switch (xmlReader.getEventType()) {
        case XMLStreamConstants.START_ELEMENT:
          highlightStartElement(xmlReader, startLocation);
          break;

        case XMLStreamConstants.END_ELEMENT:
          highlightEndElement(xmlReader, prevLocation, startLocation);
          break;

        case XMLStreamConstants.CDATA:
          highlightCData(startLocation);
          break;

        case XMLStreamConstants.DTD:
          highlightDTD(startLocation);
          break;

        case XMLStreamConstants.COMMENT:
          addUnclosedHighlighting(startLocation, TypeOfText.STRUCTURED_COMMENT);
          break;

        default:
          break;
      }
    }
  }

  private void highlightDTD(XmlLocation startLocation) throws XMLStreamException {
    XmlLocation closingBracketStartOffset = startLocation.moveBefore(">");
    addHighlighting(startLocation, startLocation.shift(9), TypeOfText.STRUCTURED_COMMENT);
    addHighlighting(closingBracketStartOffset, closingBracketStartOffset.shift(1), TypeOfText.STRUCTURED_COMMENT);
  }

  private void highlightCData(XmlLocation startLocation) throws XMLStreamException {
    if (!startLocation.startsWith("<![CDATA[")) {
      // Ignoring secondary CDATA event
      // See https://docs.oracle.com/javase/7/docs/api/javax/xml/stream/XMLStreamReader.html#next()
      return;
    }

    addHighlighting(startLocation, startLocation.moveAfter("<![CDATA["), TypeOfText.KEYWORD);

    // highlight "]]>"
    XmlLocation beforeClosingTag = startLocation.moveBefore("]]>");
    addHighlighting(beforeClosingTag, beforeClosingTag.moveAfter("]]>"), TypeOfText.KEYWORD);
  }

  private void highlightEndElement(XMLStreamReader xmlReader, XmlLocation prevLocation, XmlLocation startLocation) throws XMLStreamException {
    XmlLocation closingBracketStartLocation = startLocation.moveBefore(">");
    XmlLocation currentLocation = new XmlLocation(content, xmlReader.getLocation());
    boolean isEmptyElement = prevLocation.isSameAs(currentLocation);

    if (isEmptyElement) {
      // empty (or autoclosing) element is raised twice as start and end element,
      // so we need to highlight closing "/" which is placed just before ">"
      addHighlighting(closingBracketStartLocation.moveBackward(), closingBracketStartLocation, TypeOfText.KEYWORD);
    } else {
      addHighlighting(startLocation, closingBracketStartLocation.shift(1), TypeOfText.KEYWORD);
    }
  }

  private void highlightStartElement(XMLStreamReader xmlReader, XmlLocation startLocation) throws XMLStreamException {
    XmlLocation closingBracketStartLocation = startLocation.moveBefore(">");
    int nameWithNamespaceLength = getNameWithNamespaceLength(xmlReader) + 1;
    XmlLocation endLocation = startLocation.shift(nameWithNamespaceLength);

    addHighlighting(startLocation, endLocation, TypeOfText.KEYWORD);
    highlightAttributes(endLocation, closingBracketStartLocation);
    addHighlighting(closingBracketStartLocation, closingBracketStartLocation.shift(1), TypeOfText.KEYWORD);
  }

  private void highlightXmlDeclaration() throws XMLStreamException {
    XmlLocation startLocation = new XmlLocation(content);
    if (startLocation.startsWith(XML_DECLARATION_TAG)) {
      // always starts from origin
      XmlLocation closingBracketLocation = startLocation.moveBefore(">");

      XmlLocation nextLocation = startLocation.moveAfter(XML_DECLARATION_TAG);
      addHighlighting(startLocation, nextLocation, TypeOfText.KEYWORD);
      highlightAttributes(nextLocation, closingBracketLocation);
      addHighlighting(closingBracketLocation.moveBackward(), closingBracketLocation.shift(1), TypeOfText.KEYWORD);
    }
  }

  private void highlightAttributes(XmlLocation start, XmlLocation end) throws XMLStreamException {
    XmlLocation currentLocation = start.moveAfterWhitespaces();

    while (currentLocation.has("=", end)) {
      XmlLocation attributeNameEnd = currentLocation.moveBefore("=");
      addHighlighting(currentLocation, attributeNameEnd, TypeOfText.CONSTANT);

      XmlLocation attributeValueStart = attributeNameEnd.moveAfter("=").moveAfterWhitespaces();
      char c = attributeValueStart.readChar();
      XmlLocation attributeValueEnd = attributeValueStart.shift(1).moveAfter(String.valueOf(c));
      addHighlighting(attributeValueStart, attributeValueEnd, TypeOfText.STRING);

      currentLocation = attributeValueEnd.moveAfterWhitespaces();
    }
  }

  private static int getNameWithNamespaceLength(XMLStreamReader streamReader) {
    int prefixLength = 0;
    if (!streamReader.getName().getPrefix().isEmpty()) {
      prefixLength = streamReader.getName().getPrefix().length() + 1;
    }

    return prefixLength + streamReader.getLocalName().length();
  }

  private void addHighlighting(XmlLocation start, XmlLocation end, TypeOfText typeOfText) {
    if (start.isSameAs(end)) {
      throw new IllegalArgumentException("Cannot highlight an empty range");
    }
    int startLine = start.line() + xmlFileStartLocation.line() - 1;
    int startColumn = start.column() + (start.line() == xmlFileStartLocation.line() ? (xmlFileStartLocation.column() - 1) : 0);
    int endLine = end.line() + xmlFileStartLocation.line() - 1;
    int endColumn = end.column() + (end.line() == xmlFileStartLocation.line() ? (xmlFileStartLocation.column() - 1) : 0);
    highlighting.add(new HighlightingData(startLine, startColumn, endLine, endColumn, typeOfText));
  }

  private void addUnclosedHighlighting(XmlLocation startLocation, TypeOfText code) {
    currentStartLocation = startLocation;
    currentCode = code;
  }

  private void closeHighlighting(XmlLocation endLocation) {
    if (currentCode != null) {
      addHighlighting(currentStartLocation, endLocation, currentCode);
      currentCode = null;
    }
  }

}
