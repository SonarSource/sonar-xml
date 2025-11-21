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
package org.sonar.plugins.xml.checks.struts;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;
import org.w3c.dom.Node;

@Rule(key = "S3374")
@DeprecatedRuleKey(repositoryKey = "java", ruleKey = "S3374")
public class FormNameDuplicationCheck extends SimpleXPathBasedCheck {

  private XPathExpression formsetsExpression = getXPathExpression("form-validation/formset");
  private XPathExpression formsExpression = getXPathExpression("form");

  @Override
  public void scanFile(XmlFile xmlFile) {
    evaluateAsList(formsetsExpression, xmlFile.getNamespaceUnawareDocument())
      .forEach(this::checkIfDuplicate);
  }

  private void checkIfDuplicate(Node formSet) {
    Map<String, Node> formsByName = new HashMap<>();
    evaluateAsList(formsExpression, formSet)
      .forEach(form -> {
        String name = getNameAttribute(form);
        Node original = formsByName.get(name);
        if (original == null) {
          formsByName.put(name, form);
        } else {
          reportIssue(form, original);
        }
      });
  }

  private void reportIssue(Node duplicate, Node original) {
    String msg = "Rename this form; line " + XmlFile.nodeLocation(original).getStartLine() + " holds another form declaration with the same name.";
    List<Secondary> secondaries = Collections.singletonList(new Secondary(original, "original"));
    reportIssue(XmlFile.nodeLocation(duplicate), msg, secondaries);
  }

  @CheckForNull
  private static String getNameAttribute(Node form) {
    Node name = XmlFile.nodeAttribute(form, "name");
    if (name != null) {
      return name.getNodeValue();
    }
    // node has no "name" attribute
    return null;
  }

}
