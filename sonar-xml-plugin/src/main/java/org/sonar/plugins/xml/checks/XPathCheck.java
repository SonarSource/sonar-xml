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
package org.sonar.plugins.xml.checks;

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.PrefixResolverDefault;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * RSPEC-140.
 */
@Rule(key = "XPathCheck")
public class XPathCheck extends AbstractXmlCheck {

  @RuleProperty(key = "expression", description = "The XPath query", type = "TEXT")
  private String expression;

  @RuleProperty(key = "filePattern", description = "The files to be validated using Ant-style matching patterns")
  private String filePattern;

  @RuleProperty(
    key = "message",
    description = "The issue message",
    defaultValue = "The XPath expression matches this piece of code")
  private String message;

  private static final class DocumentNamespaceContext implements NamespaceContext {

    private final PrefixResolver resolver;

    private DocumentNamespaceContext(PrefixResolver resolver) {
      this.resolver = resolver;
    }

    @Override
    public String getNamespaceURI(String prefix) {
      return resolver.getNamespaceForPrefix(prefix);
    }

    @Override
    // Dummy implementation - not used!
    public String getPrefix(String uri) {
      return null;
    }

    @Override
    // Dummy implementation - not used!
    public Iterator<Object> getPrefixes(String val) {
      return null;
    }
  }

  private void evaluateXPath() {
    Document document = getWebSourceCode().getDocument(expression.contains(":"));
    XPathExpression xPathExpression = getXPathExpressionForDocument(document);

    try {
      evaluateXPathForNodeSet(document, xPathExpression);
    } catch (XPathExpressionException exceptionNodeSet) {
      evaluateXPathForBoolean(document, xPathExpression);
    }

  }

  private void evaluateXPathForNodeSet(Document document, XPathExpression xPathExpression) throws XPathExpressionException {
    NodeList nodes = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);

    for (int i = 0; i < nodes.getLength(); i++) {
      int lineNumber = getWebSourceCode().getLineForNode(nodes.item(i));
      createViolation(lineNumber);
    }
  }

  private void evaluateXPathForBoolean(Document document, XPathExpression xPathExpression) {
    try {
      Boolean result = (Boolean) xPathExpression.evaluate(document, XPathConstants.BOOLEAN);
      if (result) {
        // File level issue
        createViolation(null);
      }

    } catch (XPathExpressionException exceptionBoolean) {
      throw createExpressionException(exceptionBoolean);
    }
  }

  private void createViolation(Integer lineNumber) {
    if (message == null) {
      createViolation(lineNumber, "--");
    } else {
      createViolation(lineNumber, message);
    }
  }

  public String getExpression() {
    return expression;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public String getMessage() {
    return message;
  }

  private XPathExpression getXPathExpressionForDocument(Document document) {
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      PrefixResolver resolver = new PrefixResolverDefault(document.getDocumentElement());
      xpath.setNamespaceContext(new DocumentNamespaceContext(resolver));
      return xpath.compile(expression);
    } catch (XPathExpressionException e) {
      throw createExpressionException(e);
    }
  }

  private IllegalStateException createExpressionException(XPathExpressionException e) {
    return new IllegalStateException(
      String.format("Can't compile XPath expression \"%s\" for rule %s", expression, getRuleKey()), e);
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public void setFilePattern(String filePattern) {
    this.filePattern = filePattern;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public void validate(XmlSourceCode xmlSourceCode) {
    setWebSourceCode(xmlSourceCode);

    if (expression != null && isFileIncluded(filePattern)) {
      evaluateXPath();
    }
  }
}
