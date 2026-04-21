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
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class AndroidPermissionsCheckTest {

  @Test
  void manifest() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("AndroidManifest.xml").toString(), new AndroidPermissionsCheck());
  }

  @Test
  void not_manifest() {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get("AnyFileName.xml").toString(), new AndroidPermissionsCheck());
  }

  @Test
  void should_not_raise_on_tools_node_remove() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("ToolsNodeRemove/AndroidManifest.xml").toString(), new AndroidPermissionsCheck());
  }

  @Test
  void should_not_raise_on_tools_node_remove_all() {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get("ToolsNodeRemoveAll/AndroidManifest.xml").toString(), new AndroidPermissionsCheck());
  }

  @Test
  void should_raise_on_tools_node_remove_all_with_wrong_tag() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("ToolsNodeRemoveAllWrongTag/AndroidManifest.xml").toString(), new AndroidPermissionsCheck());
  }
}
