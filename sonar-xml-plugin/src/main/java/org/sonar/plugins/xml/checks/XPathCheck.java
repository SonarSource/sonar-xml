/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.util.Collections;
import java.util.Iterator;
import javax.annotation.CheckForNull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * RSPEC-140.
 */
@Rule(key = XPathCheck.RULE_KEY)
public class XPathCheck extends SonarXmlCheck {

  private static final Logger LOG = Loggers.get(XPathCheck.class);

  public static final String RULE_KEY = "XPathCheck";

  @RuleProperty(key = "expression", description = "The XPath query", type = "TEXT")
  private String expression;

  @RuleProperty(key = "filePattern", description = "The files to be validated using Ant-style matching patterns")
  private String filePattern;

  @RuleProperty(
    key = "message",
    description = "The issue message",
    defaultValue = "The XPath expression matches this piece of code")
  private String message;

  @CheckForNull
  private Boolean requiresNamespace = null;

  @Override
  public void scanFile(XmlFile file) {
    if (!isFileIncluded(file)) {
      return;
    }

    XPathExpression xPathExpression = getXPathExpression(file);

    Document document = requiresNamespace() ? file.getNamespaceAwareDocument() : file.getNamespaceUnawareDocument();
    try {
      NodeList nodes = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); i++) {
        reportIssue(nodes.item(i), getMessage());
      }

    } catch (XPathExpressionException nodeSetException) {
      try {
        Boolean result = (Boolean) xPathExpression.evaluate(document, XPathConstants.BOOLEAN);
        if (Boolean.TRUE.equals(result)) {
          reportIssueOnFile(getMessage(), Collections.emptyList());
        }
      } catch (XPathExpressionException booleanException) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(String.format("[%s] Unable to evaluate XPath expression '%s' on file %s", ruleKey(), expression, inputFile().toString()));
          LOG.error("Xpath exception:", booleanException);
        }
      }
    }
  }

  private boolean requiresNamespace() {
    if (requiresNamespace == null) {
      requiresNamespace = false;
      // '::' can be used for various xpath operator
      for(String subExpr: expression.split("::")) {
        // presence of ':' identifying a namespace requirement
        if (subExpr.contains(":")
        // explicit requirement of namespaces
        || subExpr.contains("namespace-uri")) {
          requiresNamespace = true;
        }
      }
    }
    return requiresNamespace;
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

  public String getMessage() {
    if (message != null && !message.trim().isEmpty()) {
      return message;
    }
    return "Change this XML node to not match: " + expression;
  }

  private XPathExpression getXPathExpression(XmlFile file) {
    XPathExpression xPathExpression;
    XPath xpath = XPathFactory.newInstance().newXPath();
    PrefixResolver resolver = new PrefixResolver(file.getDocument().getDocumentElement());
    xpath.setNamespaceContext(new DocumentNamespaceContext(resolver));
    try {
      xPathExpression = xpath.compile(expression);
    } catch (XPathExpressionException e) {
      throw new IllegalStateException("Failed to compile XPath expression based on user-provided parameter [" + expression + "]", e);
    }
    return xPathExpression;
  }

  private boolean isFileIncluded(XmlFile file) {
    return filePattern == null || WildcardPattern.create(filePattern).match(file.getInputFile().absolutePath());
  }

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
    public Iterator<String> getPrefixes(String val) {
      return null;
    }
  }

  /**
   * An internal re-implementation of the PrefixResolver from the xalan-j library.
   */
  private static class PrefixResolver {
    Node mContext;

    public PrefixResolver(Node xpathExpressionContext) {
      mContext = xpathExpressionContext;
    }

    public String getNamespaceForPrefix(String prefix) {
      return getNamespaceForPrefix(prefix, mContext);
    }

    @CheckForNull
    public String getNamespaceForPrefix(String prefix, Node namespaceContext) {
      if ("xml".equals(prefix)) {
        return "http://www.w3.org/XML/1998/namespace";
      }

      for (Node current = namespaceContext; current != null; current = current.getParentNode()) {
        if (current.getNodeName().indexOf(prefix + ":") == 0) {
          return current.getNamespaceURI();
        }
        NamedNodeMap attributes = current.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
          Node attr = attributes.item(i);
          if (prefix.equals(extractPrefixFromAttribute(attr))) {
            return attr.getNodeValue();
          }
        }
      }
      /*
      Because of current test setup, we cannot create an XML file where a prefix's namespace is looked up but not found.
      For this reason, the next line is not covered by our unit tests.
       */
      return null;
    }

    @CheckForNull
    private static String extractPrefixFromAttribute(Node attr) {
      String attrName = attr.getNodeName();
      if (attrName.startsWith("xmlns:")) {
        return attrName.substring(attrName.indexOf(':') + 1);
      }
      return null;
    }
  }
}
