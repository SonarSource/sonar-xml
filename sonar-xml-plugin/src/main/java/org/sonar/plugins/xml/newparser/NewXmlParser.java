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
package org.sonar.plugins.xml.newparser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.sonar.plugins.xml.newparser.NewXmlFile.Location;
import org.sonar.plugins.xml.newparser.PrologElement.PrologAttribute;
import org.sonar.plugins.xml.parsers.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class NewXmlParser {

  private static final String BOM_CHAR = "\ufeff";
  private static final String XML_DECLARATION_TAG = "<?xml";

  private XmlLocation xmlFileStartLocation;
  private XmlLocation currentNodeStartLocation = null;
  private XmlTextRange currentNodeStartRange = null;
  private String content;

  // latest processed node
  private Node currentNode;
  private boolean currentNodeIsClosed = false;
  private boolean previousEventIsText = false;
  private Deque<Node> nodes = new LinkedList<>();
  private NewXmlFile xmlFile;

  public NewXmlParser(NewXmlFile xmlFile, boolean namespaceAware) {
    this.xmlFile = xmlFile;
    try {
      setContent();
      ByteArrayInputStream stream = new ByteArrayInputStream(content.getBytes(xmlFile.getCharset()));
      Document document = getDocumentBuilder(namespaceAware).parse(stream);
      xmlFile.setDocument(document, namespaceAware);
      currentNode = document;
      nodes.push(currentNode);

      parseXmlDeclaration();
      parseXml();

      setDocumentLocation(xmlFile);

    } catch (XMLStreamException|SAXException|IOException|ParserConfigurationException e) {
      throw new ParseException(e);
    }
  }

  private static void setDocumentLocation(NewXmlFile xmlFile) {
    Document document = xmlFile.getDocument();
    XmlTextRange startRange = NewXmlFile.nodeLocation(document.getFirstChild());
    XmlTextRange end = NewXmlFile.nodeLocation(document.getLastChild());
    Optional<PrologElement> prologElement = xmlFile.getPrologElement();
    if (prologElement.isPresent()) {
      startRange = prologElement.get().getPrologStartLocation();
    }
    document.setUserData(Location.NODE.name(), new XmlTextRange(startRange, end), null);
  }

  private void setContent() throws XMLStreamException {
    String fullContent = xmlFile.getContents();

    if (fullContent.startsWith(BOM_CHAR)) {
      // remove it immediately
      fullContent = fullContent.substring(1);
    }
    int realStartIndex = fullContent.indexOf(XML_DECLARATION_TAG);

    if (realStartIndex == -1) {
      xmlFileStartLocation = new XmlLocation(fullContent);
      content = fullContent;
    } else {
      content = fullContent.substring(realStartIndex);
      xmlFileStartLocation = new XmlLocation(fullContent).moveBefore(XML_DECLARATION_TAG);
    }
  }

  private void parseXml() throws XMLStreamException {
    XMLStreamReader xmlReader = getXmlStreamReader();

    while (xmlReader.hasNext()) {
      previousEventIsText = xmlReader.getEventType() == XMLStreamConstants.CHARACTERS;
      xmlReader.next();
      XmlLocation startLocation = new XmlLocation(content, xmlReader.getLocation());

      finalizePreviousNode(startLocation);

      switch (xmlReader.getEventType()) {
        case XMLStreamConstants.COMMENT:
          visitComment(startLocation);
          break;

        case XMLStreamConstants.CHARACTERS:
          visitTextNode(startLocation);
          break;

        case XMLStreamConstants.START_ELEMENT:
          visitStartElement(xmlReader, startLocation);
          break;

        case XMLStreamConstants.END_ELEMENT:
          visitEndElement(startLocation);
          break;

        case XMLStreamConstants.CDATA:
          visitCdata(startLocation);
          break;

        case XMLStreamConstants.DTD:
          visitDTD(startLocation);
          break;

        default:
          break;
      }

      if (xmlReader.getEventType() != XMLStreamConstants.START_ELEMENT
        && xmlReader.getEventType() != XMLStreamConstants.END_ELEMENT) {
        // as no end event for non-element nodes, consider them closed
        currentNodeIsClosed = true;
      }
    }
  }

  private void visitComment(XmlLocation startLocation) {
    setNextNode();
    currentNodeStartLocation = startLocation;
  }

  private void visitTextNode(XmlLocation startLocation) {
    if (previousEventIsText) {
      // text can appear after another text when it's not coalesced (see XMLInputFactory.IS_COALESCING)
      // so both events stand for the same node in DOM
      currentNodeStartRange = NewXmlFile.nodeLocation(currentNode);
    } else {
      setNextNode();
      currentNodeStartLocation = startLocation;
    }
  }

  private void finalizePreviousNode(XmlLocation endLocation) {
    if (currentNodeStartLocation != null) {
      setLocation(currentNode, Location.NODE, currentNodeStartLocation, endLocation);
    } else if (currentNodeStartRange != null) {
      currentNode.setUserData(Location.NODE.name(), new XmlTextRange(currentNodeStartRange, endLocation, xmlFileStartLocation), null);
    }

    currentNodeStartLocation = null;
    currentNodeStartRange = null;
  }

  private XMLStreamReader getXmlStreamReader() throws XMLStreamException {
    Reader reader = new StringReader(content);

    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
    return factory.createXMLStreamReader(reader);
  }

  private static DocumentBuilder getDocumentBuilder(boolean namespaceAware) throws ParserConfigurationException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    documentBuilderFactory.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", false);
    documentBuilderFactory.setValidating(false);
    documentBuilderFactory.setExpandEntityReferences(false);
    documentBuilderFactory.setNamespaceAware(namespaceAware);
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    // Implementations of DocumentBuilder usually provide Error Handlers, which may add some extra logic, such as logging.
    // This line disable these custom handlers during parsing, as we don't need it
    documentBuilder.setErrorHandler(null);
    return documentBuilder;
  }

  private void visitStartElement(XMLStreamReader xmlReader, XmlLocation startLocation) throws XMLStreamException {
    setNextNode();
    nodes.push(currentNode);
    XmlLocation nameEndLocation = startLocation.shift(getNameWithNamespaceLength(xmlReader) + 1);
    XmlLocation closingBracketEndLocation = startLocation.moveAfter(">");
    setLocation(currentNode, Location.START, startLocation, closingBracketEndLocation);
    setLocation(currentNode, Location.NAME, startLocation.shift(1), nameEndLocation);
    visitAttributes(nameEndLocation, closingBracketEndLocation.moveBackward());
  }

  private void visitEndElement(XmlLocation startLocation) throws XMLStreamException {
    currentNode = nodes.pop();
    XmlLocation closingBracketEndLocation = startLocation.moveAfter(">");
    setLocation(currentNode, Location.END, startLocation, closingBracketEndLocation);
    XmlTextRange startRange = (XmlTextRange) currentNode.getUserData(Location.START.name());
    currentNode.setUserData(Location.NODE.name(), new XmlTextRange(startRange, closingBracketEndLocation, xmlFileStartLocation), null);
    currentNodeIsClosed = true;
  }

  private void setNextNode() {
    if (currentNodeIsClosed) {
      // when currentNode (last processed node) is closed, it's impossible that we visit its child
      currentNode = currentNode.getNextSibling();
    } else {
      currentNode = currentNode.getFirstChild();
    }

    currentNodeIsClosed = false;
  }

  private void parseXmlDeclaration() throws XMLStreamException {
    XmlLocation startLocation = new XmlLocation(content);
    if (startLocation.startsWith(XML_DECLARATION_TAG)) {
      XmlLocation endLocation =  startLocation.moveAfter(">");
      XmlLocation attributesStart = startLocation.moveAfter(XML_DECLARATION_TAG);

      List<PrologAttribute> prologAttributes = visitPrologAttributes(attributesStart, endLocation.moveBackward());

      xmlFile.setPrologElement(new PrologElement(
        prologAttributes,
        new XmlTextRange(startLocation, attributesStart, xmlFileStartLocation),
        new XmlTextRange(endLocation.moveBackward().moveBackward(), endLocation, xmlFileStartLocation)
        ));
    }
  }

  private void visitDTD(XmlLocation startLocation) throws XMLStreamException {
    setNextNode();
    XmlLocation endLocation = startLocation.moveAfter(">");
    setLocation(currentNode, Location.NODE, startLocation, endLocation);
  }

  private void visitCdata(XmlLocation startLocation) throws XMLStreamException {
    if (!startLocation.startsWith("<![CDATA[")) {
      // Ignoring secondary CDATA event
      // See https://docs.oracle.com/javase/7/docs/api/javax/xml/stream/XMLStreamReader.html#next()
      return;
    }
    setNextNode();

    XmlLocation beforeClosingTag = startLocation.moveBefore("]]>");
    XmlLocation endLocation = beforeClosingTag.moveAfter("]]>");
    setLocation(currentNode, Location.START, startLocation, startLocation.moveAfter("<![CDATA["));
    setLocation(currentNode, Location.END, beforeClosingTag, endLocation);
    setLocation(currentNode, Location.NODE, startLocation, endLocation);
  }

  private void setLocation(Node node, Location locationKind, XmlLocation start, XmlLocation end) {
    node.setUserData(locationKind.name(), new XmlTextRange(start, end, xmlFileStartLocation), null);
  }

  private void visitAttributes(XmlLocation start, XmlLocation end) throws XMLStreamException {
    NamedNodeMap attributes = currentNode.getAttributes();
    int attrIndex = 0;
    XmlLocation currentLocation = start.moveAfterWhitespaces();

    while (currentLocation.has("=", end)) {
      XmlLocation attributeNameEnd = currentLocation.moveBefore("=");

      XmlLocation attributeValueStart = attributeNameEnd.moveAfter("=").moveAfterWhitespaces();
      char c = attributeValueStart.readChar();
      XmlLocation attributeValueEnd = attributeValueStart.shift(1).moveAfter(String.valueOf(c));

      Node attr = attributes.item(attrIndex);
      setLocation(attr, Location.NAME, currentLocation, attributeNameEnd);
      setLocation(attr, Location.VALUE, attributeValueStart, attributeValueEnd);
      setLocation(attr, Location.NODE, currentLocation, attributeValueEnd);

      currentLocation = attributeValueEnd.moveAfterWhitespaces();
      attrIndex++;
    }
  }

  private List<PrologAttribute> visitPrologAttributes(XmlLocation start, XmlLocation end) throws XMLStreamException {
    XmlLocation currentLocation = start.moveAfterWhitespaces();
    List<PrologAttribute> attributes = new ArrayList<>();

    while (currentLocation.has("=", end)) {
      XmlLocation attributeNameEnd = currentLocation.moveBefore("=");

      XmlLocation attributeValueStart = attributeNameEnd.moveAfter("=").moveAfterWhitespaces();
      char c = attributeValueStart.readChar();
      XmlLocation attributeValueEnd = attributeValueStart.shift(1).moveAfter(String.valueOf(c));

      attributes.add(new PrologAttribute(
        currentLocation.textUntil(attributeNameEnd),
        new XmlTextRange(currentLocation, attributeNameEnd, xmlFileStartLocation),
        removeQuotes(attributeValueStart.textUntil(attributeValueEnd)),
        new XmlTextRange(attributeValueStart, attributeValueEnd, xmlFileStartLocation)
      ));
      currentLocation = attributeValueEnd.moveAfterWhitespaces();
    }

    return attributes;
  }

  private static String removeQuotes(String str) {
    if ((str.startsWith("\"") || str.startsWith("'")) && str.length() > 1) {
      return str.substring(1, str.length() - 1);
    }

    return str;
  }

  private static int getNameWithNamespaceLength(XMLStreamReader streamReader) {
    int prefixLength = 0;
    if (!streamReader.getName().getPrefix().isEmpty()) {
      prefixLength = streamReader.getName().getPrefix().length() + 1;
    }

    return prefixLength + streamReader.getLocalName().length();
  }
}
