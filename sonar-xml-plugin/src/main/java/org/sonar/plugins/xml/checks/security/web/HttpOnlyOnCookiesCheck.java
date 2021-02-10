/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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

import java.util.List;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonar.plugins.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Node;

@Rule(key = "S3330")
public class HttpOnlyOnCookiesCheck extends AbstractWebXmlCheck {

  private XPathExpression sessionConfigCookieConfigExpression = XPathBuilder
    .forExpression("/n:web-app/n:session-config/n:cookie-config")
    .withNamespace("n", "http://xmlns.jcp.org/xml/ns/javaee")
    .build();

  private XPathExpression httpOnlyExpression = XPathBuilder.forExpression("n:http-only")
    .withNamespace("n", "http://xmlns.jcp.org/xml/ns/javaee")
    .build();

  @Override
  void scanWebXml(XmlFile file) {
    evaluateAsList(sessionConfigCookieConfigExpression, file.getDocument()).forEach(this::checkHttpOnly);
  }

  private void checkHttpOnly(Node cookieConfig) {
    List<Node> httpOnlyNodes = evaluateAsList(httpOnlyExpression, cookieConfig);
    if (httpOnlyNodes.isEmpty()) {
      reportIssue(cookieConfig, "<http-only> tag is missing and should be set to true.");
    } else {
      httpOnlyNodes.stream()
        .filter(HttpOnlyOnCookiesCheck::isNotSetToTrue)
        .forEach(this::reportWrongValue);
    }

  }

  private static boolean isNotSetToTrue(Node node) {
    return !"true".equals(node.getTextContent());
  }

  private void reportWrongValue(Node node) {
    reportIssue(node, "<http-only> tag should be set to true.");
  }

}
