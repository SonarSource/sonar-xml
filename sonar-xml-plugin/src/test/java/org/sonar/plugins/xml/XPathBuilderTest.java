/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.plugins.xml;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XPathBuilderTest {

  @Test
  void document_without_namespace() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
    Document doc = parse("<b></b>");

    XPathExpression xPathWithoutNameSpace = XPathBuilder.forExpression("/b").build();
    Node firstNode = (Node)xPathWithoutNameSpace.evaluate(doc, XPathConstants.NODE);
    assertThat(firstNode).isNotNull();
    assertThat(firstNode.getPrefix()).isNull();
    assertThat(firstNode.getLocalName()).isEqualTo("b");
    assertThat(firstNode.getNamespaceURI()).isNull();

    XPathExpression xPathWithNameSpace = XPathBuilder.forExpression("/n1:b")
      .withNamespace("n1", "http://my.namespace")
      .build();
    firstNode = (Node)xPathWithNameSpace.evaluate(doc, XPathConstants.NODE);
    assertThat(firstNode).isNull();
  }

  @Test
  void document_with_default_namespace() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
    Document doc = parse("<b xmlns=\"http://my.namespace\"></b>");

    XPathExpression xPathWithoutNameSpace = XPathBuilder.forExpression("/b").build();
    Node firstNode = (Node)xPathWithoutNameSpace.evaluate(doc, XPathConstants.NODE);
    assertThat(firstNode).isNull();

    XPathExpression xPathWithNameSpace = XPathBuilder.forExpression("/n1:b")
      .withNamespace("n1", "http://my.namespace")
      .build();
    firstNode = (Node)xPathWithNameSpace.evaluate(doc, XPathConstants.NODE);
    assertThat(firstNode).isNotNull();
    assertThat(firstNode.getPrefix()).isNull();
    assertThat(firstNode.getLocalName()).isEqualTo("b");
    assertThat(firstNode.getNamespaceURI()).isEqualTo("http://my.namespace");
  }

  @Test
  void document_with_prefixed_namespace() throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
    Document doc = parse("<foo:b xmlns:foo=\"http://my.namespace\"></foo:b>");

    XPathExpression xPathWithoutNameSpace = XPathBuilder.forExpression("/b").build();
    Node firstNode = (Node)xPathWithoutNameSpace.evaluate(doc, XPathConstants.NODE);
    assertThat(firstNode).isNull();

    XPathExpression xPathWithNameSpace = XPathBuilder.forExpression("/n1:b")
      .withNamespace("n1", "http://my.namespace")
      .build();
    firstNode = (Node)xPathWithNameSpace.evaluate(doc, XPathConstants.NODE);
    assertThat(firstNode).isNotNull();
    assertThat(firstNode.getPrefix()).isEqualTo("foo");
    assertThat(firstNode.getLocalName()).isEqualTo("b");
    assertThat(firstNode.getNamespaceURI()).isEqualTo("http://my.namespace");
  }

  @Test
  void undefined_xpath_prefix() {
    XPathBuilder xPathBuilder = XPathBuilder.forExpression("/n1:a/n2:b").withNamespace("n1", "http://n1");
    assertThatThrownBy(xPathBuilder::build)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageStartingWith("Failed to compile XPath expression [/n1:a/n2:b]: ");
  }

  @Test
  void xpath_context() {
    XPathBuilder.XPathContext xPathContext = new XPathBuilder.XPathContext();
    xPathContext.add("n", "foo-namespace");
    assertThat(xPathContext.getNamespaceURI("n")).isEqualTo("foo-namespace");
    assertThat(xPathContext.getNamespaceURI("x")).isEmpty();
    assertThatThrownBy(() -> xPathContext.getPrefix("foo-namespace"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageStartingWith("Only provides 'getNamespaceURI(prefix)' conversion");
    assertThatThrownBy(() -> xPathContext.getPrefixes("foo-namespace"))
      .isInstanceOf(UnsupportedOperationException.class)
      .hasMessageStartingWith("Only provides 'getNamespaceURI(prefix)' conversion");
  }

  private static Document parse(String xml) throws IOException, SAXException, ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new InputSource(new StringReader(xml)));
  }

}
