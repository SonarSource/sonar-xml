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
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.squidbridge.annotations.RuleTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Rule(
  key = "XPathCheck",
  name = "XPath rule",
  priority = Priority.MAJOR)
@RuleTemplate
public class XPathCheck extends AbstractXmlCheck {

  @RuleProperty(key = "expression", description = "The XPath query", type = "TEXT")
  private String expression;

  @RuleProperty(key = "filePattern", description = "Files to be validated using Ant-style matching patterns.")
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
