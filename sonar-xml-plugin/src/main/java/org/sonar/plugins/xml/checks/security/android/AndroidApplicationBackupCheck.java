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

import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Rule(key = "S6358")
public class AndroidApplicationBackupCheck extends AbstractAndroidManifestCheck {

  private static final String MESSAGE = "Make sure backup of application data is safe here.";
  private static final String BASE_XPATH_QUERY = "/manifest/application" +
    "[" +
    " not(@n:allowBackup='false')" +
    "and" +
    " not(@n:backupAgent)" +
    "%s" +
    "]";
  private static final String APPLICATION_WITH_BACKUP_QUERY = String.format(BASE_XPATH_QUERY,
    "and not(@n:fullBackupContent and (starts-with(@n:fullBackupContent, '@') or starts-with(@n:fullBackupContent, '$')))"
    );
  private static final String APPLICATION_BELOW_SDK_23_QUERY = String.format(BASE_XPATH_QUERY, "");

  private static final XPathExpression X_PATH_APPLICATION_WITH_BACKUP = XPathBuilder
    .forExpression(APPLICATION_WITH_BACKUP_QUERY)
    .withNamespace("n", "http://schemas.android.com/apk/res/android")
    .build();

  private static final XPathExpression X_PATH_APPLICATION_BELOW_SDK_23 = XPathBuilder
    .forExpression(APPLICATION_BELOW_SDK_23_QUERY)
    .withNamespace("n", "http://schemas.android.com/apk/res/android")
    .build();

  @Override
  protected void scanAndroidManifest(XmlFile file) {
    List<Node> nodes;
    if(minSdkVersion != null && minSdkVersion < 23) {
      nodes = evaluateAsList(X_PATH_APPLICATION_BELOW_SDK_23, file.getDocument());
    } else {
      nodes = evaluateAsList(X_PATH_APPLICATION_WITH_BACKUP, file.getDocument());
    }
    nodes.forEach(node -> reportIssue(XmlFile.nameLocation((Element) node), MESSAGE, Collections.emptyList()));
  }


}
