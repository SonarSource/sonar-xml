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

import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class AndroidClearTextCheckTest {

  @Test
  void test() {
    SonarXmlCheckVerifier.verifyIssues("AndroidManifest.xml", new AndroidClearTextCheck());
  }

  @Test
  void not_manifest() {
    SonarXmlCheckVerifier.verifyNoIssue("NotManifest.xml", new AndroidClearTextCheck());
  }

  @Test
  void does_not_raise_when_usesCleartextTraffic_is_not_set_and_minSdk_greater_or_equal_to_28() {
    SonarXmlCheckVerifier.verifyNoIssue(
      "implicit/AndroidManifest.xml",
      new AndroidClearTextCheck(),
      new MapSettings().setProperty("sonar.android.minsdkversion.min", 28)
    );
  }

  @Test
  void raises_when_usesCleartextTraffic_is_not_set_and_minSdk_is_less_than_28() {
    SonarXmlCheckVerifier.verifyIssues(
      "implicit/AndroidManifest.xml",
      new AndroidClearTextCheck(),
      new MapSettings().setProperty("sonar.android.minsdkversion.min", 27)
    );
  }

}
