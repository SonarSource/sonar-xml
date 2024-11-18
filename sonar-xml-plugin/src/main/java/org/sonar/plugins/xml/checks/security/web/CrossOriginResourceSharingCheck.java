/*
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
 * along with this program; if not, see https://www.sonarsource.com/legal/
 */
package org.sonar.plugins.xml.checks.security.web;

import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;

@Rule(key = "S5122")
public class CrossOriginResourceSharingCheck extends AbstractWebXmlCheck {

  private static final Pattern STAR_IN_COMMA_SEPARATED_LIST_REGEX = Pattern.compile("(^|,)\\*(,|$)");

  private XPathExpression corsAllowedOrigins = XPathBuilder
    .forExpression("/j:web-app" +
      "/j:filter[j:filter-class='org.apache.catalina.filters.CorsFilter']" +
      "/j:init-param[j:param-name='cors.allowed.origins']" +
      "/j:param-value" +
      "/text()")
    .withNamespace("j", "http://xmlns.jcp.org/xml/ns/javaee")
    .build();

  @Override
  void scanWebXml(XmlFile file) {
    evaluateAsList(corsAllowedOrigins, file.getDocument()).stream()
      .filter(node -> STAR_IN_COMMA_SEPARATED_LIST_REGEX.matcher(node.getNodeValue()).find())
      .forEach(node -> reportIssue(node, "Make sure this permissive CORS policy is safe here."));
  }

}
