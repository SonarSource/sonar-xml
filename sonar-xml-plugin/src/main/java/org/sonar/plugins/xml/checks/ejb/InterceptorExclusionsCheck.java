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
package org.sonar.plugins.xml.checks.ejb;

import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;
import org.w3c.dom.Node;

@Rule(key = "S3282")
@DeprecatedRuleKey(repositoryKey = "java", ruleKey = "S3282")
public class InterceptorExclusionsCheck extends SimpleXPathBasedCheck {

  private XPathExpression notDefaultInterceptorBindingsExpression = getXPathExpression("ejb-jar/assembly-descriptor/interceptor-binding[ejb-name!=\"*\"]");
  private XPathExpression exclusionsExpression = getXPathExpression("*[self::exclude-default-interceptors[text()=\"true\"] or self::exclude-class-interceptors[text()=\"true\"]]");

  @Override
  public void scanFile(XmlFile xmlFile) {
    evaluateAsList(notDefaultInterceptorBindingsExpression, xmlFile.getNamespaceUnawareDocument()).forEach(this::checkExclusions);
  }

  private void checkExclusions(Node interceptorBinding) {
    evaluateAsList(exclusionsExpression, interceptorBinding)
      .forEach(node -> reportIssue(node, "Move this exclusion into the class as an annotation."));
  }
}
