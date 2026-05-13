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
package org.sonar.plugins.xml.checks.security.web;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class MimeNosniffCheckTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "webconfig-compliant",
    "webconfig-location-block",
    "webconfig-nosniff-whitespace",
  })
  void compliant(String dirName) {
    String path = Paths.get(dirName, "web.config").toString();
    SonarXmlCheckVerifier.verifyNoIssue(path, new MimeNosniffCheck());
  }

  @Test
  void noUploadConfigurationDoesNotRaise() {
    String path = Paths.get("webconfig-no-upload", "web.config").toString();
    SonarXmlCheckVerifier.verifyNoIssue(path, new MimeNosniffCheck());
  }

  @Test
  void uploadConfigWithoutHeaderRaisesIssue() {
    String path = Paths.get("webconfig-missing-nosniff", "web.config").toString();
    SonarXmlCheckVerifier.verifyIssues(path, new MimeNosniffCheck());
  }

  @Test
  void iisNativeMaxContentLengthRaisesIssue() {
    String path = Paths.get("webconfig-iis-max-content-length", "web.config").toString();
    SonarXmlCheckVerifier.verifyIssues(path, new MimeNosniffCheck());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "webconfig-no-custom-headers",
    "webconfig-missing-nosniff",
    "webconfig-other-value"
  })
  void noncompliant(String dirName) {
    String path = Paths.get(dirName, "web.config").toString();
    SonarXmlCheckVerifier.verifyIssues(path, new MimeNosniffCheck());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "webconfig-add-remove",
    "webconfig-add-clear",
  })
  void false_negatives(String dirName) {
    String path = Paths.get(dirName, "web.config").toString();
    SonarXmlCheckVerifier.verifyNoIssue(path, new MimeNosniffCheck());
  }
}
