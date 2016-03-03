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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse XML files and add linenumbers in the document.
 *
 * @author Matthijs Galesloot
 */
public final class SaxParser extends AbstractParser {

  private static final Logger LOG = LoggerFactory.getLogger(SaxParser.class);

  /**
   * From http://will.thestranathans.com/post/1026712315/getting-line-numbers-from-xpath-in-java
   */
  private static final class LocationRecordingHandler extends DefaultHandler implements LexicalHandler {

    private final Document doc;
    private Locator locator;
    private Element current;

    private final Map<String, String> prefixMappings = new HashMap<>();

    // The docs say that parsers are "highly encouraged" to set this
    public LocationRecordingHandler(Document doc) {
      this.doc = doc;
    }

    // Even with text nodes, we can record the line and column number
    @Override
    public void characters(char[] buf, int offset, int length) {
      if (current != null) {
        Node n = doc.createTextNode(new String(buf, offset, length));
        setLocationData(n);
        current.appendChild(n);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      Node parent;

      if (current == null) {
        return;
      }

      parent = current.getParentNode();
      // If the parent is the document itself, then we're done.
      if (parent.getParentNode() == null) {
        current.normalize();
        current = null;
      } else {
        current = (Element) current.getParentNode();
      }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    // This just takes the location info from the locator and puts
    // it into the provided node
    private void setLocationData(Node n) {
      if (locator != null) {
        n.setUserData(KEY_LINE_NO, locator.getLineNumber(), null);
        n.setUserData(KEY_COLUMN_NO, locator.getColumnNumber(), null);
      }
    }

    // Admittedly, this is largely lifted from other examples
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) {
      Element e;
      if (localName != null && !"".equals(localName)) {
        e = doc.createElementNS(uri, localName);
      } else {
        e = doc.createElement(qName);
      }

      // But this part isn't lifted ;)
      setLocationData(e);

      if (current == null) {
        doc.appendChild(e);
      } else {
        current.appendChild(e);
      }
      current = e;

      for (Entry<String, String> entry : prefixMappings.entrySet()) {
        Attr attr = doc.createAttribute("xmlns:" + entry.getKey());
        attr.setValue(entry.getValue());
        current.setAttributeNodeNS(attr);
      }
      prefixMappings.clear();

      // For each attribute, make a new attribute in the DOM, append it
      // to the current element, and set the column and line numbers.
      if (attrs != null) {
        for (int i = 0; i < attrs.getLength(); i++) {
          final Attr attr;
          if (attrs.getLocalName(i) != null && !"".equals(attrs.getLocalName(i))) {
            attr = doc.createAttributeNS(attrs.getURI(i), attrs.getLocalName(i));
            current.setAttributeNodeNS(attr);
          } else {
            attr = doc.createAttribute(attrs.getQName(i));
            current.setAttributeNode(attr);
          }
          attr.setValue(attrs.getValue(i));
          setLocationData(attr);
        }
      }
    }

    /**
     * prefixMappings (namespaces) are sent before startElement(). So keep the mappings in a Map and add as attributes in startElement().
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
      prefixMappings.put(prefix, uri);
    }

    /**
     * Comment node (needed for white space checking).
     */
    @Override
    public void comment(char[] buf, int offset, int length) throws SAXException {
      Node n = doc.createComment(new String(buf, offset, length));
      setLocationData(n);
      if (current == null) {
        doc.appendChild(n);
      } else {
        current.appendChild(n);
      }
    }

    @Override
    public void endCDATA() throws SAXException {
      // empty - Lexical Handler method
    }

    @Override
    public void endDTD() throws SAXException {
      // empty - Lexical Handler method
    }

    @Override
    public void endEntity(String arg0) throws SAXException {
      // empty - Lexical Handler method
    }

    @Override
    public void startCDATA() throws SAXException {
      // empty - Lexical Handler method
    }

    @Override
    public void startDTD(String arg0, String arg1, String arg2) throws SAXException {
      // empty - Lexical Handler method
    }

    @Override
    public void startEntity(String arg0) throws SAXException {
      // empty - Lexical Handler method
    }
  }

  private static final String KEY_LINE_NO = "saxParser.lineNumber";

  private static final String KEY_COLUMN_NO = "saxParser.columnNumber";

  /**
   * Gets the LineNumber of a node.
   */
  public static int getLineNumber(Node node) {
    Integer lineNumber = (Integer) node.getUserData(SaxParser.KEY_LINE_NO);
    return lineNumber == null ? 0 : lineNumber;
  }

  private void parse(InputStream input, DefaultHandler handler, boolean namespaceAware) throws IOException, SAXException {
    SAXParser parser = newSaxParser(namespaceAware);
    // read comments too, so use lexical handler.
    parser.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", handler);
    parser.parse(input, handler);
  }

  public Document parseDocument(String originalFilePath, InputStream input, boolean namespaceAware) {
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      LocationRecordingHandler handler = new LocationRecordingHandler(document);
      parse(input, handler, namespaceAware);
      return document;
    } catch (Exception e) {
      LOG.warn("Unable to anayle file {}", originalFilePath);
      LOG.warn("Cause: {}", e.toString());
      return null;
    }
  }

}
