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

class AndroidApplicationBackupCheckTest {



  @Test
  void test() {
    SonarXmlCheckVerifier.verifyIssues("AndroidManifest.xml", new AndroidApplicationBackupCheck());
  }

  @Test
  void test_sdk_below_23() {
    AndroidApplicationBackupCheck check = new AndroidApplicationBackupCheck();
    MapSettings settings = new MapSettings().setProperty("sonar.android.minsdkversion.min", "22");
    SonarXmlCheckVerifier.verifyIssues("AndroidApplicationBelowSDK23/AndroidManifest.xml", check, settings);
  }

  @Test
  void test_sdk_23_or_greater(){
    AndroidApplicationBackupCheck check = new AndroidApplicationBackupCheck();
    MapSettings settings = new MapSettings().setProperty("sonar.android.minsdkversion.min", "23");

    SonarXmlCheckVerifier.verifyNoIssue("AndroidApplicationBelowSDK23/AndroidManifest.xml", check, settings);
  }

  @Test
  void not_manifest() {
    SonarXmlCheckVerifier.verifyNoIssue("NotManifest.xml", new AndroidApplicationBackupCheck());
  }

  @ParameterizedTest
  @ValueSource(strings = {"library", "hascode", "tools-only", "src/debug", "src/test", "src/androidTest"})
  void does_not_raise_on_non_primary_manifest(String dir) {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get(dir, "AndroidManifest.xml").toString(), new AndroidApplicationBackupCheck());
  }

  @ParameterizedTest
  @ValueSource(strings = {"src/main", "src/release", "nolauncher-true", "label-nolauncher", "native-launcher", "app-shell"})
  void still_raises(String dir) {
    SonarXmlCheckVerifier.verifyIssues(Paths.get(dir, "AndroidManifest.xml").toString(), new AndroidApplicationBackupCheck());
  }

}
