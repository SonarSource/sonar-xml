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
package org.sonar.plugins.xml.checks.maven;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.xml.xpath.XPathExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.xml.checks.maven.helpers.MavenDependencyMatcher;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Rule(key = DisallowedDependenciesCheck.KEY)
@DeprecatedRuleKey(repositoryKey = "java", ruleKey = DisallowedDependenciesCheck.KEY)
public class DisallowedDependenciesCheck extends SimpleXPathBasedCheck {

  private static final Logger LOG = LoggerFactory.getLogger(DisallowedDependenciesCheck.class);

  public static final String KEY = "S3417";

  private final XPathExpression dependencyExpression = getXPathExpression("//dependencies/dependency");
  private final XPathExpression propertiesExpression = getXPathExpression("//properties");
  private final Pattern propertyPlaceholderPattern = Pattern.compile("\\$\\{(?<property>[^}]++)}");

  @RuleProperty(
    key = "dependencyName",
    description = "Pattern describing forbidden dependencies group and artifact ids. E.G. '``*:.*log4j``' or '``x.y:*``'")
  public String dependencyName = "";

  @RuleProperty(
    key = "version",
    description = "Dependency version pattern or dash-delimited range. Leave blank for all versions. E.G. '``1.3.*``', '``1.0-3.1``', '``1.0-*``' or '``*-3.1``'")
  public String version = "";

  private boolean needToInitializedMatcher = true;
  @CheckForNull
  private MavenDependencyMatcher dependencyMatcher = null;

  @Override
  public void scanFile(XmlFile xmlFile) {
    if (!"pom.xml".equalsIgnoreCase(xmlFile.getInputFile().filename())) {
      return;
    }
    MavenDependencyMatcher matcher = getMatcher();
    if (matcher == null) {
      return;
    }
    Map<String, String> propertiesMap = new HashMap<>();

    evaluateAsList(propertiesExpression, xmlFile.getNamespaceUnawareDocument())
            .forEach(properties -> XmlFile.children(properties).stream()
                    .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                    .forEach(property -> propertiesMap.put(property.getNodeName(), property.getTextContent())));

    evaluateAsList(dependencyExpression, xmlFile.getNamespaceUnawareDocument()).forEach(dependency -> {
      String groupId = getChildElementText("groupId", dependency);
      String artifactId = getChildElementText("artifactId", dependency);
      String dependencyVersion = resolveDependencyVersion(propertiesMap, dependency);

      if (matcher.matches(groupId, artifactId, dependencyVersion)) {
        reportIssue(dependency, "Remove this forbidden dependency.");
      }
    });
  }

  private String resolveDependencyVersion(Map<String, String> propertiesMap, Node dependency) {
    String dependencyVersion = getChildElementText("version", dependency);

    Matcher placeholderMatcher = propertyPlaceholderPattern.matcher(dependencyVersion);
    if (placeholderMatcher.matches()) {
      dependencyVersion = propertiesMap.getOrDefault(placeholderMatcher.group("property"), "");
    }
    return dependencyVersion;
  }

  private static String getChildElementText(String childElementName, Node parent) {
    for (Node node : XmlFile.children(parent)) {
      if (node.getNodeType() == Node.ELEMENT_NODE && ((Element) node).getTagName().equals(childElementName)) {
        return node.getTextContent();
      }
    }

    return "";
  }

  private MavenDependencyMatcher getMatcher() {
    if (needToInitializedMatcher) {
      needToInitializedMatcher = false;
      try {
        dependencyMatcher = new MavenDependencyMatcher(dependencyName, version);
      } catch (RuntimeException e) {
        LOG.error("The rule xml:{} is configured with some invalid parameters. {}", KEY, e.getMessage());
      }
    }
    return dependencyMatcher;
  }
}
