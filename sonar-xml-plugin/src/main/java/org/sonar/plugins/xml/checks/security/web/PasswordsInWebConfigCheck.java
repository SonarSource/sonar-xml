/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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

import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpression;
import java.util.Optional;
import java.util.stream.Stream;

@Rule(key = "S5344")
public class PasswordsInWebConfigCheck extends BaseWebCheck {

  private final XPathExpression credentialsExpression = XPathBuilder
    .forExpression("//credentials")
    .build();

  @Override
  protected void scanWebConfig(XmlFile file) {
    evaluateAsList(credentialsExpression, file.getDocument()).stream()
      .flatMap(PasswordsInWebConfigCheck::getSensitivePasswordFormat)
      .forEach(node -> reportIssue(node, "Passwords should not be stored in plain text."));
  }

  private static Stream<Node> getSensitivePasswordFormat(Node node) {
    return Optional.ofNullable(node.getAttributes().getNamedItem("passwordFormat"))
      .filter(attr -> "clear".equalsIgnoreCase(attr.getNodeValue()))
      .stream();
  }
}
