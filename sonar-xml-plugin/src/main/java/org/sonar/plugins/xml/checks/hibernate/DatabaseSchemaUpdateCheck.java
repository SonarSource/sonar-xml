/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks.hibernate;

import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Rule(key = "S3822")
@DeprecatedRuleKey(repositoryKey = "java", ruleKey = "S3822")
public class DatabaseSchemaUpdateCheck extends SimpleXPathBasedCheck {

  private XPathExpression hibernateHbm2ddlAutoProperty = getXPathExpression("//property[@name='hibernate.hbm2ddl.auto']");

  @Override
  public void scanFile(XmlFile file) {
    evaluateAsList(hibernateHbm2ddlAutoProperty, file.getNamespaceUnawareDocument()).forEach(this::checkProperty);
  }

  private void checkProperty(Node property) {
    NodeList children = property.getChildNodes();
    if (children.getLength() == 1) {
      String value = children.item(0).getNodeValue().trim();
      if (!"none".equals(value) && !"validate".equals(value)) {
        reportIssue(property, "Use \"validate\" or remove this property.");
      }
    }
  }
}
