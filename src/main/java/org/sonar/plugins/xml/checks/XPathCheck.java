/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
 * dev@sonar.codehaus.org
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
import org.sonar.api.utils.SonarException;
import org.sonar.check.Cardinality;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.xml.parsers.SaxParser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

@Rule(key = "XPathCheck", name = "XPath Check", description = "XPath Check", priority = Priority.MAJOR, cardinality = Cardinality.MULTIPLE)
public class XPathCheck extends AbstractPageCheck {

  private static final class DocumentNamespaceContext implements NamespaceContext {

    private final PrefixResolver resolver;

    private DocumentNamespaceContext(PrefixResolver resolver) {
      this.resolver = resolver;
    }

    public String getNamespaceURI(String prefix) {
      return resolver.getNamespaceForPrefix(prefix);
    }

    // Dummy implementation - not used!
    public String getPrefix(String uri) {
      return null;
    }

    // Dummy implementation - not used!
    public Iterator<Object> getPrefixes(String val) {
      return null;
    }
  }

  @RuleProperty(key = "expression", description = "Expression")
  private String expression;

  @RuleProperty(key = "filePattern", description = "File Include Pattern")
  private String filePattern;

  @RuleProperty(key = "message", description = "Message")
  private String message;

  private void evaluateXPath() {

    Document document = getWebSourceCode().getDocument(expression.contains(":"));

    if (document == null) {
      createViolation(null, "XPath check cannot be evaluated because document is not valid");
    } else {
      try {
        NodeList nodes = (NodeList) getXPathExpressionForDocument(document).evaluate(document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {

          int lineNumber = SaxParser.getLineNumber(nodes.item(i));
          if (message == null) {
            createViolation(lineNumber);
          } else {
            createViolation(lineNumber, message);
          }
        }
      } catch (XPathExpressionException e) {
        throw new SonarException(e);
      }
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
    if (expression != null) {
      try {
        XPath xpath = XPathFactory.newInstance().newXPath();
        PrefixResolver resolver = new PrefixResolverDefault(document.getDocumentElement());
        xpath.setNamespaceContext(new DocumentNamespaceContext(resolver));
        return xpath.compile(expression);
      } catch (XPathExpressionException e) {
        throw new SonarException(e);
      }
    }
    return null;
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
