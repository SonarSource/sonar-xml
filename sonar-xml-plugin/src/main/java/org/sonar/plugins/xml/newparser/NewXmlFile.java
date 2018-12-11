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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.xml.Utils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NewXmlFile {

  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private static final String ELEMENT = "element";

  public enum Location {
    NODE,
    START,
    END,
    NAME,
    VALUE
  }

  private InputFile inputFile;
  private Document documentNamespaceAware;
  // set lazely in getDocument when called with "false" argument
  private Document documentNamespaceUnaware;
  private String contents;
  private Charset charset;

  void setDocument(Document document, boolean namespaceAware) {
    if (namespaceAware) {
      documentNamespaceAware = document;
    } else {
      documentNamespaceUnaware = document;
    }
  }

  void setPrologElement(PrologElement prologElement) {
    this.prologElement = prologElement;
  }

  private PrologElement prologElement = null;

  private NewXmlFile(InputFile inputFile) throws IOException {
    this.inputFile = inputFile;
    this.contents = inputFile.contents();
    this.charset = inputFile.charset();
  }

  private NewXmlFile(String str) {
    this.inputFile = null;
    this.contents = str;
    this.charset = DEFAULT_CHARSET;
  }

  public static NewXmlFile create(InputFile inputFile) throws IOException {
    NewXmlFile xmlFile = new NewXmlFile(inputFile);
    new NewXmlParser(xmlFile, true);
    return xmlFile;
  }

  public static NewXmlFile create(String str) {
    NewXmlFile xmlFile = new NewXmlFile(str);
    new NewXmlParser(xmlFile, true);
    return xmlFile;
  }

  /**
   * @return null when created based on string
   */
  @Nullable
  public InputFile getInputFile() {
    return inputFile;
  }

  public String getContents() {
    return contents;
  }

  public List<String> lines() {
    return Arrays.asList(Utils.splitLines(contents));
  }

  public Charset getCharset() {
    return charset;
  }

  /**
   * @return document with namespace information
   */
  public Document getDocument() {
    return getNamespaceAwareDocument();
  }

  public Document getNamespaceAwareDocument() {
    return documentNamespaceAware;
  }

  public Document getNamespaceUnawareDocument() {
    if (documentNamespaceUnaware == null) {
      new NewXmlParser(this, false);
    }

    return documentNamespaceUnaware;
  }

  public Optional<PrologElement> getPrologElement() {
    return Optional.ofNullable(prologElement);
  }

  public static XmlTextRange startLocation(CDATASection node) {
    return getRangeOrThrow(node, Location.START, "CDATA");
  }

  public static XmlTextRange endLocation(CDATASection node) {
    return getRangeOrThrow(node, Location.END, "CDATA");
  }

  public static XmlTextRange startLocation(Element node) {
    return getRangeOrThrow(node, Location.START, ELEMENT);
  }

  public static XmlTextRange endLocation(Element node) {
    return getRangeOrThrow(node, Location.END, ELEMENT);
  }

  public static XmlTextRange nameLocation(Element node) {
    return getRangeOrThrow(node, Location.NAME, ELEMENT);
  }

  public static XmlTextRange attributeNameLocation(Attr node) {
    return getRangeOrThrow(node, Location.NAME, "attribute");
  }

  public static XmlTextRange attributeValueLocation(Attr node) {
    return getRangeOrThrow(node, Location.VALUE, "attribute");
  }

  public static XmlTextRange nodeLocation(Node node) {
    return getRangeOrThrow(node, Location.NODE, "");
  }

  public static Optional<XmlTextRange> getRange(Node node, Location location) {
    return Optional.ofNullable((XmlTextRange) node.getUserData(location.name()));
  }

  private static XmlTextRange getRangeOrThrow(Node node, Location location, String nodeType) {
    return getRange(node, location)
      .orElseThrow(() -> new IllegalStateException(String.format("Missing %s location on XML %s node", location.name().toLowerCase(Locale.ENGLISH), nodeType)));
  }

  public static List<Node> children(Node node) {
    NodeList childNodes = node.getChildNodes();
    List<Node> result = new ArrayList<>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      result.add(childNodes.item(i));
    }

    return result;
  }
}
