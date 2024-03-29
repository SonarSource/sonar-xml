/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.xml.checks.security.android;

import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;

@Rule(key = "S6359")
public class AndroidCustomPermissionCheck extends AbstractAndroidManifestCheck {

  private static final String MESSAGE = "Use a different namespace for the \"%s\" permission.";
  private final XPathExpression xPathExpression = XPathBuilder.forExpression("/manifest/permission/@n1:name")
    .withNamespace("n1", "http://schemas.android.com/apk/res/android")
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
