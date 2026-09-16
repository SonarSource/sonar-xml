/*
 * SonarQube XML Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

  // Find the global <httpCookies httpOnlyCookies="true" /> in .NET web.config.
  private final XPathExpression httpCookiesExpression = XPathBuilder
    .forExpression("/configuration/system.web/httpCookies[@httpOnlyCookies=\"true\"]")
    .build();

  // Closest existing node if the global <httpCookies> is missing or misconfigured.
  private final XPathExpression reportNodeExpression = XPathBuilder
    .forExpression(getDeepestExistingNode("configuration", "system.web", "httpCookies"))
    .build();

  // Detects a .NET Framework web.config by the presence of any known <system.web> child element.
  // See https://learn.microsoft.com/en-us/previous-versions/dotnet/netframework-4.0/dayb112d(v=vs.100)
  private final XPathExpression dotNetFrameworkSystemWebExpression = XPathBuilder
    .forExpression("/configuration/system.web[" +
      "anonymousIdentification or authentication or authorization or browserCaps or " +
      "caching or clientTarget or compilation or customErrors or deployment or " +
      "deviceFilters or globalization or healthMonitoring or hostingEnvironment or " +
      "httpCookies or httpHandlers or httpModules or httpRuntime or identity or " +
      "machineKey or membership or mobileControls or pages or processModel or " +
      "profile or roleManager or securityPolicy or sessionPageState or sessionState or " +
      "siteMap or trace or trust or urlMappings or webControls or webParts or " +
      "webServices or xhtmlConformance]")
    .build();

  @Override
  protected void scanWebXml(XmlFile file) {
    evaluateAsList(sessionConfigCookieConfigExpression, file.getDocument()).forEach(this::checkHttpOnly);
  }

  @Override
  protected void scanWebConfig(XmlFile file) {
    Document document = file.getDocument();

    // Only flag .NET Framework web.config files. ASP.NET Core web.config files do not use
    // <system.web>/<httpCookies> and should not be checked.
    NodeList systemWebNodes = evaluate(dotNetFrameworkSystemWebExpression, document);
    if (systemWebNodes == null || systemWebNodes.getLength() == 0) {
      return;
    }

    NodeList httpCookiesNodes = evaluate(httpCookiesExpression, document);

    // null is returned on internal errors, and we don't want to raise a false positive in that case.
    if (httpCookiesNodes != null && httpCookiesNodes.getLength() == 0) {
      evaluateAsList(reportNodeExpression, document)
        .stream()
        .findFirst()
        .ifPresent(target ->
          reportIssue(
            XmlFile.nameLocation((Element) target),
            "Global <httpCookies> tag is missing or its 'httpOnlyCookies' attribute is not set to true.",
            Collections.emptyList()));
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
