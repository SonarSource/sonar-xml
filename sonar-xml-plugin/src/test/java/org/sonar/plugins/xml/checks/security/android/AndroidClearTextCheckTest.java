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

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.scanner.plugin.api.impl.config.MapSettings;
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

  private static final MapSettings OLD_SDK = new MapSettings().setProperty("sonar.android.minsdkversion.min", 27);

  @ParameterizedTest
  @ValueSource(strings = {"library", "hascode", "tools-only", "src/debug", "src/test", "src/androidTest", "src/debug-true/src/debug"})
  void does_not_raise_on_non_primary_manifest(String dir) {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get(dir, "AndroidManifest.xml").toString(), new AndroidClearTextCheck(), OLD_SDK);
  }

  @ParameterizedTest
  @ValueSource(strings = {"src/main", "src/release", "nolauncher-true", "leanback", "nsc-nolauncher", "label-nolauncher", "native-launcher"})
  void still_raises(String dir) {
    SonarXmlCheckVerifier.verifyIssues(Paths.get(dir, "AndroidManifest.xml").toString(), new AndroidClearTextCheck(), OLD_SDK);
  }

}
