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

import org.junit.jupiter.api.Test;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.ConfigurationBridge;
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
    Configuration analysisConfig = new ConfigurationBridge(
      new MapSettings().setProperty("sonar.android.minsdkversion.min", "22"));
    check.readAnalysisConfiguration(analysisConfig);
    SonarXmlCheckVerifier.verifyIssues("AndroidApplicationBelowSDK23/AndroidManifest.xml", check);
  }

  @Test
  void not_manifest() {
    SonarXmlCheckVerifier.verifyNoIssue("NotManifest.xml", new AndroidApplicationBackupCheck());
  }

}
