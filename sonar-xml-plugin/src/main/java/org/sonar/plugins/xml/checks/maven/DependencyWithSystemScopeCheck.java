/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.xml.checks.maven;

import java.util.Collections;
import java.util.Optional;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Rule(key = "S3422")
@DeprecatedRuleKey(repositoryKey = "java", ruleKey = "S3422")
public class DependencyWithSystemScopeCheck extends SimpleXPathBasedCheck {

  private XPathExpression dependencyExpression = getXPathExpression("//dependencies/dependency");

  @Override
  public void scanFile(XmlFile xmlFile) {
    if (!"pom.xml".equalsIgnoreCase(xmlFile.getInputFile().filename())) {
      return;
    }

    evaluateAsList(dependencyExpression, xmlFile.getNamespaceUnawareDocument())
      .forEach(dependency -> checkDependency((Element) dependency));
  }

  private void checkDependency(Element dependency) {
    Optional<Node> scope = getElementByName("scope", dependency);
    if (!scope.isPresent() || !"system".equalsIgnoreCase(scope.get().getTextContent())) {
      return;
    }

    Optional<Node> systemPathOptional = getElementByName("systemPath", dependency);
    if (systemPathOptional.isPresent()) {
      reportIssue(
        XmlFile.nodeLocation(scope.get()),
        "Update this scope and remove the \"systemPath\".",
        Collections.singletonList(new Secondary(systemPathOptional.get(), "Remove this")));
    } else {
      reportIssue(scope.get(), "Update this scope.");
    }
  }

  private static Optional<Node> getElementByName(String name, Element nestingElement) {
    NodeList nodeList = nestingElement.getElementsByTagName(name);
    if (nodeList.getLength() > 0) {
      return Optional.of(nodeList.item(0));
    }

    return Optional.empty();
  }
}
