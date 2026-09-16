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

import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class PasswordsInWebConfigCheckTest {

  private static final SonarXmlCheck CHECK = new PasswordsInWebConfigCheck();

  @Test
  void no_password_in_web_config() {
    SonarXmlCheckVerifier.verifyNoIssue("noPasswordInWebConfig/web.config", CHECK);
  }

  @Test
  void no_credentials_tag_in_web_config() {
    SonarXmlCheckVerifier.verifyNoIssue("noCredentialsTagInWebConfig/web.config", CHECK);
  }

  @Test
  void no_password_format_attribute() {
    SonarXmlCheckVerifier.verifyNoIssue("noPasswordFormatAttribute/web.config", CHECK);
  }

  @Test
  void passwords_in_web_config() {
    SonarXmlCheckVerifier.verifyIssues("passwordsInWebConfig/web.config", CHECK);
  }
}
