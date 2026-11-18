/*
 * SonarQube XML Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.List;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.Document;

public class Utils {
  private Utils() {
    // utility class, forbidden constructor
  }

  public static final String ANDROID_MANIFEST_XMLNS = "http://schemas.android.com/apk/res/android";
  public static final String ANDROID_MANIFEST_TOOLS = "http://schemas.android.com/tools";
  public static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";

  public static boolean isAndroidManifestFile(XmlFile file) {
    return ANDROID_MANIFEST_FILENAME.equalsIgnoreCase(file.getInputFile().filename());
  }

  private static final List<String> OVERLAY_DIRS = List.of("/src/debug/", "/src/test/", "/src/androidTest/").stream().map(d -> d + ANDROID_MANIFEST_FILENAME).toList();

  private static final XPathExpression HAS_CODE_FALSE = xpath("/manifest/application[@n:hasCode='false']");
  private static final XPathExpression HAS_LAUNCHER = xpath(
    "/manifest//intent-filter[action/@n:name='android.intent.action.MAIN'"
      + " and category[@n:name='android.intent.category.LAUNCHER' or @n:name='android.intent.category.LEANBACK_LAUNCHER']]");
  // Any app-level attribute (other than tools:*) means the file makes decisions, so keep analysing it.
  private static final XPathExpression HAS_APPLICATION_ATTRIBUTE = xpath(
    "/manifest/application/@*[namespace-uri() != '" + ANDROID_MANIFEST_TOOLS + "']");

  private static XPathExpression xpath(String expression) {
    return XPathBuilder.forExpression(expression).withNamespace("n", ANDROID_MANIFEST_XMLNS).build();
  }

  /** True for manifests located directly in the source-set root: under src/debug, src/test or src/androidTest (not src/release or other flavors). */
  public static boolean isDebugOrTestOverlay(XmlFile file) {
    String path = file.getInputFile().uri().getPath().replace('\\', '/');
    return OVERLAY_DIRS.stream().anyMatch(path::endsWith);
  }

  /** Single-file heuristic: the file is recognisably not the manifest that ships. Fails open. */
  public static boolean isLikelyNonPrimaryManifest(XmlFile file) {
    if (isDebugOrTestOverlay(file)) {
      return true;
    }
    Document document = file.getDocument();
    if (matches(HAS_LAUNCHER, document)) {
      // native-only apps legitimately set hasCode="false" and ship a launcher: keep analysing
      return false;
    }
    // ponytail: pattern 3 requires an attribute-less <application>; relax only if a corpus shows FPs worth the FN risk
    return matches(HAS_CODE_FALSE, document) || !matches(HAS_APPLICATION_ATTRIBUTE, document);
  }

  private static boolean matches(XPathExpression expression, Document document) {
    try {
      return (Boolean) expression.evaluate(document, XPathConstants.BOOLEAN);
    } catch (XPathExpressionException e) {
      return false;
    }
  }
}
