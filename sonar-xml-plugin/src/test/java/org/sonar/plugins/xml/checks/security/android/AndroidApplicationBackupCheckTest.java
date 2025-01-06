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

import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;
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

}
