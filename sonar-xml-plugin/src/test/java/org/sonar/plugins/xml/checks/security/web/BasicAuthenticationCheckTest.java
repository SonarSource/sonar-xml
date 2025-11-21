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

class BasicAuthenticationCheckTest {

  @Test
  void withoutHTTPS() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("without-https", "web.xml").toString(), new BasicAuthenticationCheck());
  }

  @Test
  void withHTTPS() {
    SonarXmlCheckVerifier.verifyNoIssue(Paths.get("with-https", "web.xml").toString(), new BasicAuthenticationCheck());
  }

  @Test
  void no_issue_if_not_web_xml() {
    SonarXmlCheckVerifier.verifyNoIssue("noweb.xml", new BasicAuthenticationCheck());
  }

}
