/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
