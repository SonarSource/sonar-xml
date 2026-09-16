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

class HttpOnlyOnCookiesCheckTest {

  @Test
  void with_namespace() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("with-namespace", "web.xml").toString(), new HttpOnlyOnCookiesCheck());
  }

  @Test
  void without_namespace() {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get("without-namespace", "web.xml").toString(), new HttpOnlyOnCookiesCheck());
  }

  @Test
  void without_prefixed_namespace() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("with-prefixed-namespace", "web.xml").toString(), new HttpOnlyOnCookiesCheck());
  }

  @Test
  void without_schema() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("without-schema", "web.xml").toString(), new HttpOnlyOnCookiesCheck());
  }

  @Test
  void no_issue_if_not_web_xml() {
    SonarXmlCheckVerifier.verifyNoIssue("noweb.xml", new HttpOnlyOnCookiesCheck());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "webconfig-without-http-cookies",
    "webconfig-with-noncompliant-http-cookies",
    "webconfig-dotnet-framework-compilation",
  })
  void web_config_noncompliant(String dirName) {
    String path = Paths.get(dirName, "web.config").toString();
    SonarXmlCheckVerifier.verifyIssues(path, new HttpOnlyOnCookiesCheck());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "webconfig-with-http-cookies-true",
    // ASP.NET Core web.config files do not use <system.web>/<httpCookies> — no issue expected.
    "webconfig-aspnetcore",
  })
  void web_config_compliant(String dirName) {
    String path = Paths.get(dirName, "web.config").toString();
    SonarXmlCheckVerifier.verifyNoIssue(path, new HttpOnlyOnCookiesCheck());
  }
}
