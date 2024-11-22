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
package org.sonar.plugins.xml.checks.maven;

import java.util.regex.Pattern;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Rule(key = GroupIdNamingConventionCheck.KEY)
@DeprecatedRuleKey(repositoryKey = "java", ruleKey = GroupIdNamingConventionCheck.KEY)
public class GroupIdNamingConventionCheck extends SimpleXPathBasedCheck {

  public static final String KEY = "S3419";

  private static final String DEFAULT_REGEX = "(com|org)(\\.[a-z][a-z-0-9]*)+";

  @RuleProperty(
    key = "regex",
    description = "The regular expression the \"groupId\" should match",
    defaultValue = "" + DEFAULT_REGEX)
  public String regex = DEFAULT_REGEX;

  private XPathExpression groupIdExpression = getXPathExpression("project/groupId");
  private Pattern pattern = null;

  @Override
  public void scanFile(XmlFile file) {
    if (!"pom.xml".equalsIgnoreCase(file.getInputFile().filename())) {
      return;
    }
    NodeList groupIds = evaluate(groupIdExpression, file.getNamespaceUnawareDocument());
    if (groupIds == null || groupIds.getLength() != 1) {
      return;
    }
    Node groupId = groupIds.item(0);
    if (!getPattern().matcher(groupId.getTextContent()).matches()) {
      reportIssue(groupId, "Update this \"groupId\" to match the provided regular expression: '" + regex + "'");
    }
  }

  private Pattern getPattern() {
    if (pattern == null) {
      try {
        pattern = Pattern.compile(regex, Pattern.DOTALL);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("[" + KEY + "] Unable to compile the regular expression: " + regex, e);
      }
    }
    return pattern;
  }

}
