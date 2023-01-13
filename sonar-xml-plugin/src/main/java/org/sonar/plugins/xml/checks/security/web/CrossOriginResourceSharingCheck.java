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
