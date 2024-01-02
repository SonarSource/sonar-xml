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
package org.sonar.plugins.xml.checks.security;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class HardcodedCredentialsCheckTest {

  private static final HardcodedCredentialsCheck CHECK = new HardcodedCredentialsCheck();

  @Test
  void tags_and_properties() {
    SonarXmlCheckVerifier.verifyIssues("tagsAndProperties.xml", CHECK);
  }

  @Test
  void entities() {
    SonarXmlCheckVerifier.verifyNoIssue("entities.xml", CHECK);
  }

  @Test
  void customized() {
    HardcodedCredentialsCheck check = new HardcodedCredentialsCheck();
    check.credentialWords = "banana , APPLE   ";
    SonarXmlCheckVerifier.verifyIssues("customized.xml", check);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "dbeaver-data-sources.xml",
    "filezilla-filezilla3-config.xml",
    "filezilla-recentservers.xml",
    "idea-WebServers.xml",
    "jenkins-BapSshHostConfiguration.xml",
    "jenkins-credentials.xml",
    "sonarqube-analysis-properties.xml",
    "sonarqube-pom.xml",
    "spring-social-all.xml",
    "spring-social-facebook-beans.xml",
    "spring-social-github-beans.xml",
    "spring-social-google-beans.xml",
    "spring-social-linkedin-beans.xml",
    "spring-social-twitter-beans.xml",
    "teiid-standalone.xml"
  })
  void special_cases(String file) {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("special-cases", file).toString(), CHECK);
  }

  @Test
  void android_password_attribute_is_ignored() {
    SonarXmlCheckVerifier.verifyNoIssue("android_password.xml", CHECK);
  }

  @Test
  void web_application() {
    SonarXmlCheckVerifier.verifyIssues(Paths.get("web-application", "web.config").toString(), CHECK);
    SonarXmlCheckVerifier.verifyIssues(Paths.get("web-application", "Machine.config").toString(), CHECK);
  }
}
