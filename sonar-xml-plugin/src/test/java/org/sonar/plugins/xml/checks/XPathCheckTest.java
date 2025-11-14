/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
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
    SonarXmlCheckVerifier.verifyIssues("with_xml_namespace.xml", getCheck("//xml:template"));
    SonarXmlCheckVerifier.verifyIssues("with_out_of_parent_namespace.xml", getCheck("//other:template"));
  }

  @Test
  void test_with_namespace_uri() {
    XPathCheck check = getCheck("//*[namespace-uri()='sap.ui.core.mvc']");
    SonarXmlCheckVerifier.verifyIssues("with_namespace_uri.xml", check);
    SonarXmlCheckVerifier.verifyNoIssue("with_namespace_uri_no_issues.xml", check);
  }

  @Test
  void test_with_default_namespace() {
    SonarXmlCheckVerifier.verifyIssues("with_default_namespaces.xml", getCheck("//template"));
  }

  @Test
  void test_with_double_colon() {
    XPathCheck check1 = getCheck("//dependency/artifactId[starts-with(text(),'mule-http-connector')]//following-sibling::version[not(starts-with(text(),'1.6'))]");
    SonarXmlCheckVerifier.verifyIssues("double_colon_with_default_namespace.xml", check1);
    SonarXmlCheckVerifier.verifyIssues("double_colon_without_default_namespace.xml", check1);

    XPathCheck check2 = getCheck("//bar//following-sibling::mvc:View[namespace-uri()='sap.ui.core.mvc']");
    SonarXmlCheckVerifier.verifyIssues("double_colon_with_namespace_in_query.xml", check2);
  }

  @Test
  void test_failure_without_log() {
    XPathCheck check = new XPathCheck();
    check.setExpression("//comment()");

    logTester.clear();
    logTester.setLevel(Level.INFO);
    SonarXmlCheckVerifier.verifyNoIssue("../xPathFailure.xml", check);
    assertThat(logTester.getLogs()).isEmpty();

    logTester.clear();
    logTester.setLevel(Level.DEBUG);
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
