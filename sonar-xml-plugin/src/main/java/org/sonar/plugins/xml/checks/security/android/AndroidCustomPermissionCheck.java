/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.plugins.xml.checks.security.android;

import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;

import static org.sonar.plugins.xml.checks.security.android.Utils.ANDROID_MANIFEST_XMLNS;

@Rule(key = "S6359")
public class AndroidCustomPermissionCheck extends AbstractAndroidManifestCheck {

  private static final String MESSAGE = "Use a different namespace for the \"%s\" permission.";
  private final XPathExpression xPathExpression = XPathBuilder.forExpression("/manifest/permission/@n1:name")
    .withNamespace("n1", ANDROID_MANIFEST_XMLNS)
    .build();

  @Override
  protected final void scanAndroidManifest(XmlFile file) {
    evaluateAsList(xPathExpression, file.getDocument()).stream()
      .filter(node -> node.getNodeValue().startsWith("android.permission"))
      .forEach(node -> reportIssue(node, String.format(MESSAGE, simpleName(node.getNodeValue()))));
  }

  private static String simpleName(String fullyQualifiedName) {
    return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
  }

}
