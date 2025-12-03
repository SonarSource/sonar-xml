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
package org.sonar.plugins.xml.checks.security.web;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
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

}
