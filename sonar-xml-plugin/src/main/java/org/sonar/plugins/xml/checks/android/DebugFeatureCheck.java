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
package org.sonar.plugins.xml.checks.android;

import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonar.plugins.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;

@Rule(key = "S4507")
public class DebugFeatureCheck extends SimpleXPathBasedCheck {

  private static final String ANDROID_MANIFEST = "AndroidManifest.xml";
  private static final String MESSAGE = "Make sure this debug feature is deactivated before delivering the code in production.";
  private final XPathExpression xPathExpression = XPathBuilder.forExpression("/manifest/application/@n1:debuggable[.='true']")
    .withNamespace("n1", "http://schemas.android.com/apk/res/android")
    .build();

  @Override
  public final void scanFile(XmlFile file) {
    if (ANDROID_MANIFEST.equalsIgnoreCase(file.getInputFile().filename())) {
      evaluateAsList(xPathExpression, file.getDocument()).stream()
        .findFirst().ifPresent(node -> reportIssue(node, MESSAGE));
    }
  }

}
