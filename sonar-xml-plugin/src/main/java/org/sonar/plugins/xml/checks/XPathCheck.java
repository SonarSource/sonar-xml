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
package org.sonar.plugins.xml.checks;

import java.util.Collections;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.PrefixResolverDefault;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.w3c.dom.Document;
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

  @Override
  public void scanFile(XmlFile file) {
    if (!isFileIncluded(file)) {
      return;
    }

    XPathExpression xPathExpression = getXPathExpression(file);

    boolean xPathRequiresNamespaces = expression.contains(":");
    Document document = xPathRequiresNamespaces ? file.getNamespaceAwareDocument() : file.getNamespaceUnawareDocument();
    try {
      NodeList nodes = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); i++) {
        reportIssue(nodes.item(i), message);
      }

    } catch (XPathExpressionException nodeSetException) {
      try {
        Boolean result = (Boolean) xPathExpression.evaluate(document, XPathConstants.BOOLEAN);
        if (result) {
          reportIssueOnFile(message, Collections.emptyList());
        }
      } catch (XPathExpressionException booleanException) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(String.format("[%s] Unable to evaluate XPath expression '%s' on file %s", ruleKey(), expression, inputFile().toString()));
          LOG.error("Xpath exception:", booleanException);
        }
      }
    }
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

  private XPathExpression getXPathExpression(XmlFile file) {
    XPathExpression xPathExpression;
    XPath xpath = XPathFactory.newInstance().newXPath();
    PrefixResolver resolver = new PrefixResolverDefault(file.getDocument().getDocumentElement());
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
    public Iterator getPrefixes(String val) {
      return null;
    }
  }
}
