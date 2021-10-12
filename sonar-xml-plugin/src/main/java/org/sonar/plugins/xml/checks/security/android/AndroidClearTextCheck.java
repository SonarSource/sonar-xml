/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
  private static final String MESSAGE_IMPLICIT = "\"usesCleartextTraffic\" is implicitly enabled for older Android version." + MESSAGE;

  private static final String ANDROID_NAME_SPACE = "http://schemas.android.com/apk/res/android";

  private final XPathExpression xPathClearTextTrue = XPathBuilder
    .forExpression("/manifest/application[@n:usesCleartextTraffic='true']")
    .withNamespace("n", ANDROID_NAME_SPACE)
    .build();

  private final XPathExpression xPathClearTextImplicit = XPathBuilder
    .forExpression("/manifest/application[not(@n:usesCleartextTraffic)]")
    .withNamespace("n", ANDROID_NAME_SPACE)
    .build();

  private final XPathExpression xPathMinVersionBelow27 = XPathBuilder
    .forExpression("/manifest/uses-sdk[@n:minSdkVersion<=27]")
    .withNamespace("n", ANDROID_NAME_SPACE)
    .build();

  @Override
  protected void scanAndroidManifest(XmlFile file) {
    Document document = file.getDocument();
    evaluateAsList(xPathClearTextTrue, document).forEach(node -> reportAtNameLocation(node, MESSAGE));
    if (shouldReportImplicitFlag(document)) {
      evaluateAsList(xPathClearTextImplicit, document).forEach(node -> reportAtNameLocation(node, MESSAGE_IMPLICIT));
    }
  }

  private void reportAtNameLocation(Node node, String message) {
    reportIssue(XmlFile.nameLocation((Element) node), message, Collections.emptyList());
  }

  private boolean shouldReportImplicitFlag(Document document) {
    // Flag is implicitly set to true when min version is <= 27.
    // In theory, when max is defined and < 24, the flag does not exist, we should not report an issue.
    // We accept it as a corner case though: it is advised to not set a max, and it would mean that the application
    // supports a maximum version that is 6 years old.
    return !evaluateAsList(xPathMinVersionBelow27, document).isEmpty();
  }

}
