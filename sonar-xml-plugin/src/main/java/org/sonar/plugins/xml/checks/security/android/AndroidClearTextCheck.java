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
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Rule(key = "S5332")
public class AndroidClearTextCheck extends AbstractAndroidManifestCheck {

  private static final String MESSAGE = "Make sure allowing clear-text traffic is safe here.";
  private static final String MESSAGE_IMPLICIT = "\"usesCleartextTraffic\" is implicitly enabled for older Android versions. " + MESSAGE;

  private static final String ANDROID_NAME_SPACE = "http://schemas.android.com/apk/res/android";

  private final XPathExpression xPathClearTextTrue = XPathBuilder
    .forExpression("/manifest/application[@n:usesCleartextTraffic='true']")
    .withNamespace("n", ANDROID_NAME_SPACE)
    .build();

  private final XPathExpression xPathClearTextImplicit = XPathBuilder
    .forExpression("/manifest/application[not(@n:usesCleartextTraffic)]")
    .withNamespace("n", ANDROID_NAME_SPACE)
    .build();

  @Override
  protected void scanAndroidManifest(XmlFile file) {
    Document document = file.getDocument();
    evaluateAsList(xPathClearTextTrue, document).forEach(node -> reportAtNameLocation(node, MESSAGE));
    Integer minSdk = getContext().config().getInt("android:minSdkVersion").orElse(27);
    // As of Android SDK 28, `usesCleartextTraffic` is implicitly set to false by default
    // See https://developer.android.com/guide/topics/manifest/application-element
    if (minSdk < 28) {
      evaluateAsList(xPathClearTextImplicit, document).forEach(node -> reportAtNameLocation(node, MESSAGE_IMPLICIT));
    }
  }

  private void reportAtNameLocation(Node node, String message) {
    reportIssue(XmlFile.nameLocation((Element) node), message, Collections.emptyList());
  }

}
