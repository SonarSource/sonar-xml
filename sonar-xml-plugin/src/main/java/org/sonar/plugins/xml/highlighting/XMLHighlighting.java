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
package org.sonar.plugins.xml.highlighting;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.xml.checks.XmlFile;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
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
  private String currentCode = null;

  private static final Logger LOG = LoggerFactory.getLogger(XMLHighlighting.class);

  public XMLHighlighting(XmlFile xmlFile, Charset charset) throws IOException {
    content = Files.toString(xmlFile.getIOFile(), charset);
    delta = xmlFile.getOffsetDelta();

    try {
      highlightXML(new InputStreamReader(new FileInputStream(xmlFile.getIOFile()), charset));
    } catch (XMLStreamException e) {
      LOG.warn("Can't highlight following file : " + xmlFile.getIOFile().getAbsolutePath(), e);
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
          addUnclosedHighlighting(startOffset, "j");
          break;

        default:
          break;
      }
    }
  }

  private void highlightDTD( int startOffset) {
    int closingBracketStartOffset;
    closingBracketStartOffset = getTagClosingBracketStartOffset(startOffset);
    addHighlighting(startOffset, startOffset + 9, "j");
    addHighlighting(closingBracketStartOffset, closingBracketStartOffset + 1, "j");
  }

  private void highlightCData(int startOffset) {
    if (!content.substring(startOffset).startsWith("<![CDATA[")) {
      // Ignoring secondary CDATA event
      // See https://docs.oracle.com/javase/7/docs/api/javax/xml/stream/XMLStreamReader.html#next()
      return;
    }

    int closingBracketStartOffset = getCDATAClosingBracketStartOffset(startOffset);

    // 9 is length of "<![CDATA["
    addHighlighting(startOffset, startOffset + 9, "k");

    // highlight "]]>"
    addHighlighting(closingBracketStartOffset - 2, closingBracketStartOffset + 1, "k");
  }

  private void highlightEndElement(XMLStreamReader xmlReader, Location prevLocation, int startOffset) {
    int closingBracketStartOffset = getTagClosingBracketStartOffset(startOffset);

    boolean isEmptyElement = prevLocation.getLineNumber() == xmlReader.getLocation().getLineNumber()
      && prevLocation.getColumnNumber() == xmlReader.getLocation().getColumnNumber();

    if (isEmptyElement) {
      // empty (or autoclosing) element is raised twice as start and end element, so we need to highlight closing "/" which is placed just before ">"
      addHighlighting(closingBracketStartOffset - 1, closingBracketStartOffset, "k");

    } else {
      addHighlighting(startOffset, closingBracketStartOffset + 1, "k");
    }
  }

  private void highlightStartElement(XMLStreamReader xmlReader, int startOffset) {
    int closingBracketStartOffset = getTagClosingBracketStartOffset(startOffset);
    int endOffset = startOffset + getNameWithNamespaceLength(xmlReader) + 1;

    addHighlighting(startOffset, endOffset, "k");
    highlightAttributes(endOffset, closingBracketStartOffset);
    addHighlighting(closingBracketStartOffset, closingBracketStartOffset + 1, "k");
  }

  private void highlightXmlDeclaration() {
    int startOffset = content.startsWith(XmlFile.BOM_CHAR) ? 1 : 0;
    if (content.startsWith(XML_DECLARATION_TAG, startOffset)) {
      int closingBracketStartOffset = getTagClosingBracketStartOffset(startOffset);

      addHighlighting(startOffset, startOffset + XML_DECLARATION_TAG.length(), "k");
      highlightAttributes(startOffset + XML_DECLARATION_TAG.length(), closingBracketStartOffset);
      addHighlighting(closingBracketStartOffset - 1, closingBracketStartOffset + 1, "k");
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
        addHighlighting(startOffset, counter + 1, "s");
        counter++;
        startOffset = null;
        attributeValueQuote = null;
      }

      if (c == '=' && attributeValueQuote == null) {
        addHighlighting(startOffset, counter, "c");

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

  private void addHighlighting(int startOffset, int endOffset, String code) {
    highlighting.add(new HighlightingData(startOffset + delta, endOffset + delta, code));
  }

  private void addUnclosedHighlighting(int startOffset, String code) {
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
