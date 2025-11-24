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

import java.util.List;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Rule(key = "S3330")
public class HttpOnlyOnCookiesCheck extends BaseWebCheck {

  private final XPathExpression sessionConfigCookieConfigExpression = XPathBuilder
    .forExpression("/n:web-app/n:session-config/n:cookie-config")
    .withNamespace("n", "http://xmlns.jcp.org/xml/ns/javaee")
    .build();

  private final XPathExpression httpOnlyExpression = XPathBuilder.forExpression("n:http-only")
    .withNamespace("n", "http://xmlns.jcp.org/xml/ns/javaee")
    .build();

  /// Find the global `<httpCookies httpOnlyCookies="true" />` in .NET web.config.
  private final XPathExpression httpCookiesExpression = XPathBuilder
    .forExpression("/configuration/system.web/httpCookies[@httpOnlyCookies=\"true\"]")
    .build();

  /// Closest existing node if the global `<httpCookies>` is missing or misconfigured.
  private final XPathExpression reportNodeExpression = XPathBuilder
    .forExpression(
      "/configuration/system.web/httpCookies | " +
      "/configuration/system.web[not(httpCookies)] | " +
      "/configuration[not(system.web)]")
    .build();

  @Override
  protected void scanWebXml(XmlFile file) {
    evaluateAsList(sessionConfigCookieConfigExpression, file.getDocument()).forEach(this::checkHttpOnly);
  }

  @Override
  protected void scanWebConfig(XmlFile file) {
    Document document = file.getDocument();
    NodeList httpCookiesNodes = evaluate(httpCookiesExpression, document);

    // null is returned on internal errors, and we don't want to raise a false positive in that case.
    if (httpCookiesNodes != null  && httpCookiesNodes.getLength() == 0) {
      evaluateAsList(reportNodeExpression, document)
        .stream()
        .findFirst()
        .ifPresent(target ->
          reportIssue(target, "Global <httpCookies> tag is missing or its 'httpOnlyCookies' attribute is not set to true."));
    }
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
