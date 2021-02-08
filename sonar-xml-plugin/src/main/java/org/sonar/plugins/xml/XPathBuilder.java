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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.sonar.api.internal.google.common.annotations.VisibleForTesting;

public class XPathBuilder {

  private static final XPathFactory FACTORY = XPathFactory.newInstance();

  private final String expression;
  private final XPathContext namespaceContext = new XPathContext();

  private XPathBuilder(String expression) {
    this.expression = expression;
  }

  public static XPathBuilder forExpression(String expression) {
    return new XPathBuilder(expression);
  }

  public XPathBuilder withNamespace(String prefix, String namespaceURI) {
    namespaceContext.add(prefix, namespaceURI);
    return this;
  }

  public XPathExpression build() {
    try {
      XPath xpath = FACTORY.newXPath();
      xpath.setNamespaceContext(namespaceContext);
      return xpath.compile(expression);
    } catch (XPathExpressionException e) {
      throw new IllegalStateException("Failed to compile XPath expression [" + expression + "]: " + e.getMessage(), e);
    }
  }

  @VisibleForTesting
  static class XPathContext implements NamespaceContext {

    private final Map<String, String> namespaceByPrefixMap = new HashMap<>();

    @VisibleForTesting
    void add(String prefix, String namespaceURI) {
      namespaceByPrefixMap.put(prefix, namespaceURI);
    }

    @Override
    public String getNamespaceURI(String prefix) {
      return namespaceByPrefixMap.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
    }

    @Override
    public String getPrefix(String namespaceURI) {
      throw new UnsupportedOperationException("Only provides 'getNamespaceURI(prefix)' conversion");
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
      throw new UnsupportedOperationException("Only provides 'getNamespaceURI(prefix)' conversion");
    }

  }
}
