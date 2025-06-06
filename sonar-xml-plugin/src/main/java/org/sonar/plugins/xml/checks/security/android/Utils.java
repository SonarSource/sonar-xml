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

import org.sonarsource.analyzer.commons.xml.XmlFile;

public class Utils {
  private Utils() {
    // utility class, forbidden constructor
  }

  public static final String ANDROID_MANIFEST_XMLNS = "http://schemas.android.com/apk/res/android";
  public static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";

  public static boolean isAndroidManifestFile(XmlFile file) {
    return ANDROID_MANIFEST_FILENAME.equalsIgnoreCase(file.getInputFile().filename());
  }
}
