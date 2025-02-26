/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks.spring;

import java.util.stream.IntStream;

import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


@Rule(key = "S3438")
@DeprecatedRuleKey(repositoryKey = "java", ruleKey = "S3438")
public class SingleConnectionFactoryCheck extends SimpleXPathBasedCheck {

  private XPathExpression singleConnectionFactoryBeansExpression = getXPathExpression("beans/bean[@class='org.springframework.jms.connection.SingleConnectionFactory']");
  private XPathExpression reconnectOnExceptionPropertyExpression = getXPathExpression("property[@name='reconnectOnException']");
  private XPathExpression valueExpression = getXPathExpression("value[text()='true']");

  @Override
  public void scanFile(XmlFile file) {
    evaluateAsList(singleConnectionFactoryBeansExpression, file.getNamespaceUnawareDocument()).forEach(bean -> {
      if (!hasAttributeValue(bean, "p:reconnectOnException") && !hasPropertyAsChild(bean, reconnectOnExceptionPropertyExpression)) {
        reportIssue(bean, "Add a \"reconnectOnException\" property, set to \"true\"");
      }
    });
  }

  private static boolean hasAttributeValue(Node bean, String attributeName) {
    NamedNodeMap attributes = bean.getAttributes();
    return IntStream.range(0, attributes.getLength())
      .mapToObj(attributes::item)
      // ignore namespace
      .anyMatch(attribute -> attribute.getNodeName().endsWith(attributeName) && "true".equals(attribute.getNodeValue()));
  }

  private boolean hasPropertyAsChild(Node bean, XPathExpression expression) {
    return evaluateAsList(expression, bean).stream()
      .anyMatch(property -> hasAttributeValue(property, "value") || evaluate(valueExpression, property).getLength() > 0);
  }

}
