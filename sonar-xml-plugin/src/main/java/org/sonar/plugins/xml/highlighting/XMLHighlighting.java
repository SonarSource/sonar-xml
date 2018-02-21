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
import javax.xml.stream.Location;
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

  private final XmlLocation xmlFileStartLocation;
  private final String content;

  public XMLHighlighting(XmlFile xmlFile) throws IOException {
    this(xmlFile.getInputFile().contents(), String.format("Can't highlight following file : %s", xmlFile.getAbsolutePath()));
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
    if (realStartIndex == -1) {
      xmlFileStartLocation = new XmlLocation();
      content = xmlStrContent;
    } else {
      xmlFileStartLocation = new XmlLocation().shift(xmlStrContent.substring(0, realStartIndex));
      content = xmlStrContent.substring(realStartIndex);
    }

    try {
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
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, "false");
    factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, "false");
    XMLStreamReader xmlReader = factory.createXMLStreamReader(reader);
    highlightXmlDeclaration();

    while (xmlReader.hasNext()) {
      XmlLocation prevLocation = new XmlLocation(xmlReader.getLocation());
      xmlReader.next();
      XmlLocation startLocation = new XmlLocation(xmlReader.getLocation());
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

  private void highlightDTD(XmlLocation startLocation) {
    XmlLocation closingBracketStartOffset = getTagClosingBracketStartOffset(startLocation);
    addHighlighting(startLocation, startLocation.shift(9), TypeOfText.STRUCTURED_COMMENT);
    addHighlighting(closingBracketStartOffset, closingBracketStartOffset.shift(1), TypeOfText.STRUCTURED_COMMENT);
  }

  private void highlightCData(XmlLocation startLocation) {
    if (!content.substring(startLocation.characterOffset).startsWith("<![CDATA[")) {
      // Ignoring secondary CDATA event
      // See https://docs.oracle.com/javase/7/docs/api/javax/xml/stream/XMLStreamReader.html#next()
      return;
    }

    XmlLocation closingBracketStartLocation = getCDATAClosingBracketStartOffset(startLocation);

    // 9 is length of "<![CDATA["
    addHighlighting(startLocation, startLocation.shift(9), TypeOfText.KEYWORD);

    // highlight "]]>"
    addHighlighting(closingBracketStartLocation.shift(-2), closingBracketStartLocation.shift(1), TypeOfText.KEYWORD);
  }

  private void highlightEndElement(XMLStreamReader xmlReader, XmlLocation prevLocation, XmlLocation startLocation) {
    XmlLocation closingBracketStartLocation = getTagClosingBracketStartOffset(startLocation);
    XmlLocation currentLocation = new XmlLocation(xmlReader.getLocation());
    boolean isEmptyElement = prevLocation.line == currentLocation.line && prevLocation.column == currentLocation.column;

    if (isEmptyElement) {
      // empty (or autoclosing) element is raised twice as start and end element,
      // so we need to highlight closing "/" which is placed just before ">"
      addHighlighting(closingBracketStartLocation.shift(-1), closingBracketStartLocation, TypeOfText.KEYWORD);
    } else {
      addHighlighting(startLocation, closingBracketStartLocation.shift(1), TypeOfText.KEYWORD);
    }
  }

  private void highlightStartElement(XMLStreamReader xmlReader, XmlLocation startLocation) {
    XmlLocation closingBracketStartLocation = getTagClosingBracketStartOffset(startLocation);
    int nameWithNamespaceLength = getNameWithNamespaceLength(xmlReader) + 1;
    XmlLocation endLocation = startLocation.shift(nameWithNamespaceLength);

    addHighlighting(startLocation, endLocation, TypeOfText.KEYWORD);
    highlightAttributes(endLocation, closingBracketStartLocation);
    addHighlighting(closingBracketStartLocation, closingBracketStartLocation.shift(1), TypeOfText.KEYWORD);
  }

  private void highlightXmlDeclaration() {
    if (content.startsWith(XML_DECLARATION_TAG)) {
      // always starts from origin
      XmlLocation startLocation = new XmlLocation();
      XmlLocation closingBracketLocation = getTagClosingBracketStartOffset(startLocation);

      XmlLocation nextLocation = startLocation.shift(XML_DECLARATION_TAG);
      addHighlighting(startLocation, nextLocation, TypeOfText.KEYWORD);
      highlightAttributes(nextLocation, closingBracketLocation);
      addHighlighting(closingBracketLocation.shift(-1), closingBracketLocation.shift(1), TypeOfText.KEYWORD);
    }
  }

  private void highlightAttributes(XmlLocation start, XmlLocation end) {
    XmlLocation currentLocation = start;
    XmlLocation previousLocation = null;
    Character attributeValueQuote = null;

    while (currentLocation.characterOffset < end.characterOffset) {
      char c = content.charAt(currentLocation.characterOffset);

      if (previousLocation == null && !Character.isWhitespace(c)) {
        previousLocation = currentLocation;
      }

      if (attributeValueQuote != null && c == attributeValueQuote) {
        // closing the attribute value
        currentLocation = currentLocation.shift(c);
        // we shifted so we need a new character
        c = content.charAt(currentLocation.characterOffset);
        addHighlighting(previousLocation, currentLocation, TypeOfText.STRING);
        previousLocation = null;
        attributeValueQuote = null;
      } else if (attributeValueQuote == null && c == '=') {
        // closing the attribute name
        addHighlighting(previousLocation, currentLocation, TypeOfText.CONSTANT);
        previousLocation = null;
      } else if (attributeValueQuote == null && (c == '\'' || c == '"')) {
        // starting the attribute value
        attributeValueQuote = c;
        previousLocation = currentLocation;
      }

      currentLocation = currentLocation.shift(c);
    }
  }

  private XmlLocation getTagClosingBracketStartOffset(XmlLocation startLocation) {
    return getClosingBracketStartOffset(startLocation, false);
  }

  private XmlLocation getCDATAClosingBracketStartOffset(XmlLocation startLocation) {
    return getClosingBracketStartOffset(startLocation, true);
  }

  private XmlLocation getClosingBracketStartOffset(XmlLocation startLocation, boolean isCDATA) {
    XmlLocation currentLocation = startLocation.shift(1);
    while (currentLocation.characterOffset < content.length()) {
      char c = content.charAt(currentLocation.characterOffset);
      if (c == '>' && bracketsBefore(isCDATA, currentLocation)) {
        return currentLocation;
      }
      currentLocation = currentLocation.shift(c);
    }

    throw new IllegalStateException("No \">\" found.");
  }

  private boolean bracketsBefore(boolean isCDATA, XmlLocation currentLocation) {
    return !isCDATA || (content.charAt(currentLocation.characterOffset - 1) == ']' && content.charAt(currentLocation.characterOffset - 2) == ']');
  }

  private static int getNameWithNamespaceLength(XMLStreamReader streamReader) {
    int prefixLength = 0;
    if (!streamReader.getName().getPrefix().isEmpty()) {
      prefixLength = streamReader.getName().getPrefix().length() + 1;
    }

    return prefixLength + streamReader.getLocalName().length();
  }

  private void addHighlighting(XmlLocation start, XmlLocation end, TypeOfText typeOfText) {
    XmlLocation newStart = start.shift(xmlFileStartLocation);
    XmlLocation newEnd = end.shift(xmlFileStartLocation);
    highlighting.add(new HighlightingData(newStart.line, newStart.column, newEnd.line, newEnd.column, typeOfText));
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

  private static class XmlLocation {

    private final int line;
    private final int column;
    private final int characterOffset;

    private XmlLocation() {
      // based on XML parser:
      // - lines start at 1
      // - columns start at at 1
      // - offset start at at 0
      this(1, 1, 0);
    }

    public XmlLocation(Location location) {
      this(location.getLineNumber(), location.getColumnNumber(), location.getCharacterOffset());
    }

    public XmlLocation(int line, int column, int characterOfffset) {
      this.line = line;
      this.column = column;
      this.characterOffset = characterOfffset;
    }

    public XmlLocation shift(String content) {
      XmlLocation result = this;
      for (char c : content.toCharArray()) {
        result = result.shift(c);
      }
      return result;
    }

    public XmlLocation shift(XmlLocation other) {
      int newLine = line + other.line - 1;
      int newColumn = line == 1 ? (column + other.column - 1) : column;
      int newOffset = characterOffset + other.characterOffset;
      return new XmlLocation(newLine, newColumn, newOffset);
    }

    public XmlLocation shift(int nbChar) {
      return new XmlLocation(line, column + nbChar, characterOffset + nbChar);
    }

    public XmlLocation shift(char c) {
      if (c == '\n') {
        return new XmlLocation(line + 1, 1, characterOffset + 1);
      }
      return new XmlLocation(line, column + 1, characterOffset + 1);
    }
  }

}
