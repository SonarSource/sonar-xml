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

import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;

import javax.xml.xpath.XPathExpression;

@Rule(key = "S5344")
public class PasswordsInWebConfigCheck extends BaseWebCheck {

  private final XPathExpression credentialsClearPassword = XPathBuilder
    .forExpression("//credentials[@passwordFormat='Clear']")
    .withNamespace("j", "http://xmlns.jcp.org/xml/ns/javaee")
    .build();

  @Override
  protected void scanWebConfig(XmlFile file) {
    evaluateAsList(credentialsClearPassword, file.getDocument()).forEach(node -> reportIssue(node, "Passwords should not be stored in plaintext."));
  }

}
