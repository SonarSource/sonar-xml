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
package org.sonar.plugins.xml.checks.security.web;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Node;

@Rule(key = "S3355")
@DeprecatedRuleKey(repositoryKey = "java", ruleKey = "S3355")
public class ValidationFiltersCheck extends AbstractWebXmlCheck {
  private XPathExpression filterNamesFromFilterExpression = getXPathExpression(WEB_XML_ROOT + "/filter/filter-name");
  private XPathExpression filterNamesFromFilterMappingExpression = getXPathExpression(WEB_XML_ROOT + "/filter-mapping/filter-name");

  @Override
  public void scanWebXml(XmlFile file) {
    Set<String> filtersInMapping = new HashSet<>();
    evaluateAsList(filterNamesFromFilterMappingExpression, file.getNamespaceUnawareDocument())
      .forEach(node -> getStringValue(node).ifPresent(filtersInMapping::add));
    evaluateAsList(filterNamesFromFilterExpression, file.getNamespaceUnawareDocument())
      .forEach(node -> {
        Optional<String> filterName = getStringValue(node);
        if (filterName.isPresent() && !filtersInMapping.contains(filterName.get())) {
          reportIssue(node, "\"" + filterName.get() + "\" should have a mapping.");
        }
      });
  }

  private static Optional<String> getStringValue(Node node) {
    Node firstChild = node.getFirstChild();
    if (firstChild == null) {
      return Optional.empty();
    }
    return Optional.of(firstChild.getNodeValue());
  }

}
