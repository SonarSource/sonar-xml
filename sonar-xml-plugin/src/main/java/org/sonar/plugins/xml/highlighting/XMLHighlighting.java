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

import java.util.ArrayList;
import java.util.List;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonarsource.analyzer.commons.xml.PrologElement;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLHighlighting {

  private List<HighlightingData> highlightingData = new ArrayList<>();

  public static List<HighlightingData> highlight(XmlFile xmlFile) {
    XMLHighlighting highlighting = new XMLHighlighting();
    xmlFile.getPrologElement().ifPresent(highlighting::highlightProlog);
    children(xmlFile.getDocument()).forEach(highlighting::highlightNode);
    return highlighting.highlightingData;
  }

  private void highlightNode(Node node) {
    switch (node.getNodeType()) {
      case Node.ELEMENT_NODE:
        highlightElementNode(node);
        break;
      case Node.CDATA_SECTION_NODE:
        addHighlighting(XmlFile.startLocation((CDATASection) node), TypeOfText.KEYWORD);
        addHighlighting(XmlFile.endLocation((CDATASection) node), TypeOfText.KEYWORD);
        break;
      case Node.COMMENT_NODE:
      case Node.DOCUMENT_TYPE_NODE:
        addHighlighting(XmlFile.nodeLocation(node), TypeOfText.STRUCTURED_COMMENT);
        break;
      default:
        break;
    }
  }

  private void highlightElementNode(Node node) {
    XmlTextRange nameLocation = XmlFile.nameLocation((Element) node);
    XmlTextRange startLocation = XmlFile.startLocation((Element) node);
    XmlTextRange endLocation = XmlFile.endLocation((Element) node);

    // <foo
    addHighlighting(startLocation, nameLocation, TypeOfText.KEYWORD);

    NamedNodeMap attributes = node.getAttributes();
    XmlTextRange lastLocation = nameLocation;
    for (int i = 0; i < attributes.getLength(); i++) {
      Attr attribute = (Attr) attributes.item(i);
      addHighlighting(XmlFile.attributeNameLocation(attribute), TypeOfText.CONSTANT);
      XmlTextRange valueLocation = XmlFile.attributeValueLocation(attribute);
      addHighlighting(valueLocation, TypeOfText.STRING);
      lastLocation = valueLocation;
    }

    // self-closing element <foo ... />
    boolean isSelfClosing = startLocation.getEndLine() == endLocation.getEndLine() && startLocation.getEndColumn() == endLocation.getEndColumn();

    if (isSelfClosing) {
      XmlTextRange textRange = new XmlTextRange(lastLocation.getEndLine(), lastLocation.getEndColumn(), endLocation.getEndLine(), endLocation.getEndColumn());
      // '/>'
      addHighlighting(textRange, TypeOfText.KEYWORD);
    } else {
      // simple element <foo> </foo>
      XmlTextRange textRange = new XmlTextRange(lastLocation.getEndLine(), lastLocation.getEndColumn(), startLocation.getEndLine(), startLocation.getEndColumn());
      // '>'
      addHighlighting(textRange, TypeOfText.KEYWORD);
      children(node).forEach(this::highlightNode);
      // '</foo>'
      addHighlighting(endLocation, TypeOfText.KEYWORD);
    }
  }

  private static List<Node> children(Node node) {
    NodeList childNodes = node.getChildNodes();
    List<Node> result = new ArrayList<>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      result.add(childNodes.item(i));
    }

    return result;
  }

  private void highlightProlog(PrologElement prologElement) {
    addHighlighting(prologElement.getPrologStartLocation(), TypeOfText.KEYWORD);

    prologElement.getAttributes().forEach(prologAttribute -> {
      addHighlighting(prologAttribute.getNameLocation(), TypeOfText.CONSTANT);
      addHighlighting(prologAttribute.getValueLocation(), TypeOfText.STRING);
    });

    addHighlighting(prologElement.getPrologEndLocation(), TypeOfText.KEYWORD);
  }

  private void addHighlighting(XmlTextRange textRange, TypeOfText typeOfText) {
    highlightingData.add(new HighlightingData(textRange, typeOfText));
  }

  private void addHighlighting(XmlTextRange start, XmlTextRange end, TypeOfText typeOfText) {
    highlightingData.add(new HighlightingData(new XmlTextRange(start, end), typeOfText));
  }

}
