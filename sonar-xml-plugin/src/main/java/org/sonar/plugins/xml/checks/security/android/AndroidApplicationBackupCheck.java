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
package org.sonar.plugins.xml.checks.security.android;

import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.sonar.plugins.xml.checks.security.android.Utils.ANDROID_MANIFEST_XMLNS;

@Rule(key = "S6358")
public class AndroidApplicationBackupCheck extends AbstractAndroidManifestCheck {

  private static final String ANDROID_MIN_SDK_VERSION = "sonar.android.minsdkversion.min";
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
    .withNamespace("n", ANDROID_MANIFEST_XMLNS)
    .build();

  private static final XPathExpression X_PATH_APPLICATION_BELOW_SDK_23 = XPathBuilder
    .forExpression(APPLICATION_BELOW_SDK_23_QUERY)
    .withNamespace("n", ANDROID_MANIFEST_XMLNS)
    .build();

  @Override
  protected void scanAndroidManifest(XmlFile file) {
    List<Node> nodes;
    Integer minSdkVersion = getContext().config().getInt(ANDROID_MIN_SDK_VERSION).orElse(null);
    if(minSdkVersion != null && minSdkVersion < 23) {
      nodes = evaluateAsList(X_PATH_APPLICATION_BELOW_SDK_23, file.getDocument());
    } else {
      nodes = evaluateAsList(X_PATH_APPLICATION_WITH_BACKUP, file.getDocument());
    }
    nodes.forEach(node -> reportIssue(XmlFile.nameLocation((Element) node), MESSAGE, Collections.emptyList()));
  }


}
