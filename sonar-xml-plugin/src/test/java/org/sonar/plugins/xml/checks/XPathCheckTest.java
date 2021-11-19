/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.plugins.xml.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnableRuleMigrationSupport
class XPathCheckTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @Test
  void test_nodes() {
    SonarXmlCheckVerifier.verifyIssues("simple.xml", getCheck("//b"));
  }

  @Test
  void test_file() {
    SonarXmlCheckVerifier.verifyIssueOnFile("simple.xml", getCheck("boolean(a)"), "XPath issue message");
    SonarXmlCheckVerifier.verifyNoIssue("simple.xml", getCheck("not(boolean(a))"));
  }

  @Test
  void test_invalid_xpath() {
    XPathCheck check = getCheck("boolean(a");
    assertThrows(IllegalStateException.class, () -> SonarXmlCheckVerifier.verifyNoIssue("simple.xml", check));
  }

  @Test
  void test_file_pattern() {
    XPathCheck check = new XPathCheck();
    check.setExpression("//b");
    check.setMessage("XPath issue message");
    check.setFilePattern("**/FOOBAR/*.xml");

    SonarXmlCheckVerifier.verifyNoIssue("simple.xml", check);
  }

  @Test
  void test_without_message() {
    XPathCheck check = new XPathCheck();
    check.setExpression("//b");
    check.setMessage(null);
    check.setFilePattern("**/*.xml");
    SonarXmlCheckVerifier.verifyIssues("without_message.xml", check);

    check.setMessage("  ");
    SonarXmlCheckVerifier.verifyIssues("without_message.xml", check);
  }

  @Test
  void test_with_namespaces() {
    SonarXmlCheckVerifier.verifyIssues("with_namespaces.xml", getCheck("//x:template"));
  }

  @Test
  void test_with_default_namespace() {
    SonarXmlCheckVerifier.verifyIssues("with_default_namespaces.xml", getCheck("//template"));
  }

  @Test
  void test_failure_without_log() {
    XPathCheck check = new XPathCheck();
    check.setExpression("//comment()");

    logTester.clear();
    logTester.setLevel(LoggerLevel.INFO);
    SonarXmlCheckVerifier.verifyNoIssue("../xPathFailure.xml", check);
    assertThat(logTester.getLogs()).isEmpty();

    logTester.clear();
    logTester.setLevel(LoggerLevel.DEBUG);
    SonarXmlCheckVerifier.verifyNoIssue("../xPathFailure.xml", check);
    assertThat(logTester.getLogs()).isNotEmpty();
  }

  private static XPathCheck getCheck(String expression) {
    XPathCheck check = new XPathCheck();
    check.setExpression(expression);
    check.setMessage("XPath issue message");
    check.setFilePattern("**/XPathCheck/*.xml");

    return check;
  }

}
