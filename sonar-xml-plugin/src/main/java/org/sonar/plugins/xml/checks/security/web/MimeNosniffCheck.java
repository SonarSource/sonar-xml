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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpression;
import java.util.Collections;

/**
 * Ensure that the X-Content-Type-Options header is set to "nosniff" to prevent MIME type sniffing.
 * The check applies to .NET web.config files.
 */
@Rule(key = "S5734")
public class MimeNosniffCheck extends BaseWebCheck {
  private final XPathExpression httpCookiesExpression = XPathBuilder
    .forExpression(
      "/configuration"
      + "/system.webServer"
      + "/httpProtocol"
      + "/customHeaders"
      + "/add[@name=\"X-Content-Type-Options\" and @value=\"nosniff\"]")
    .build();

  /** Attach the issue to the closest existing node. */
  private final XPathExpression reportNodeExpression = XPathBuilder
    .forExpression(getDeepestExistingNode("configuration", "system.webServer", "httpProtocol", "customHeaders"))
    .build();

  @Override
  protected void scanWebConfig(XmlFile file) {
    Document document = file.getDocument();
    NodeList expectedNodes = evaluate(httpCookiesExpression, document);

    // null is returned on internal errors, and we don't want to raise a false positive in that case.
    if (expectedNodes != null && expectedNodes.getLength() == 0) {
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
}
