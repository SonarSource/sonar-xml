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

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class DebugFeatureCheckTest {
  @Test
  void not_debug() {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get("not-debug", "AndroidManifest.xml").toString(), new DebugFeatureCheck());
  }

  @Test
  void debug() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("debug", "AndroidManifest.xml").toString(), new DebugFeatureCheck());
  }

  @Test
  void not_manifest() {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get("not-manifest", "AnyFileName.xml").toString(), new DebugFeatureCheck());
  }

  @Test
  void web_application_without_custom_errors() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("web-application", "web.config").toString(), new DebugFeatureCheck());
  }

  @Test
  void web_application_with_custom_errors() {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get("other-web-application", "web.config").toString(), new DebugFeatureCheck());
  }

  @Test
  void not_in_web_config() {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get("web-application", "not.a.web.config.xml").toString(), new DebugFeatureCheck());
  }

  @Test
  void debug_web_application() {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get("web-application", "web.debug.config").toString(), new DebugFeatureCheck());
  }

  @Test
  void web_server() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("web-application", "Machine.config").toString(), new DebugFeatureCheck());
  }

}
