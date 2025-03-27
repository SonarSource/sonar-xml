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

import java.util.Collections;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Element;

import static org.sonar.plugins.xml.checks.security.android.Utils.ANDROID_MANIFEST_XMLNS;

@Rule(key = "S5322")
public class AndroidReceivingIntentsCheck extends AbstractAndroidManifestCheck {

  private static final String MESSAGE = "Make sure that intents are received safely here.";

  private final XPathExpression xPathExpression = XPathBuilder
    .forExpression("/manifest/application/receiver" +
      "[" +
      " not(@n:permission)" +
      "and" +
      " not(@n:exported='false')" +
      "and" +
      " (@n:exported='true' or intent-filter)" +
      "]")
    .withNamespace("n", ANDROID_MANIFEST_XMLNS)
    .build();

  @Override
  protected void scanAndroidManifest(XmlFile file) {
    evaluateAsList(xPathExpression, file.getDocument())
      .forEach(node -> reportIssue(XmlFile.nameLocation((Element) node), MESSAGE, Collections.emptyList()));
  }

}
