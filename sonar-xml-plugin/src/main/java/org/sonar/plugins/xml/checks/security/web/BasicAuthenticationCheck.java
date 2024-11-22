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
package org.sonar.plugins.xml.checks.security.web;

import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Document;

@Rule(key = "S2647")
public class BasicAuthenticationCheck extends AbstractWebXmlCheck {

  private XPathExpression authMethodBasicExpression = XPathBuilder
    .forExpression("/j:web-app/j:login-config/j:auth-method[.='BASIC']")
    .withNamespace("j", "http://xmlns.jcp.org/xml/ns/javaee")
    .build();

  private XPathExpression httpsEnabledExpression = XPathBuilder
    .forExpression("/j:web-app/j:security-constraint/j:user-data-constraint/j:transport-guarantee[.='CONFIDENTIAL']")
    .withNamespace("j", "http://xmlns.jcp.org/xml/ns/javaee")
    .build();

  @Override
  void scanWebXml(XmlFile file) {
    Document webApp = file.getDocument();
    if (!evaluateAsList(httpsEnabledExpression, webApp).isEmpty()) {
      return;
    }
    evaluateAsList(authMethodBasicExpression, webApp).forEach(node -> reportIssue(node, "Use a more secure method than basic authentication."));
  }

}
