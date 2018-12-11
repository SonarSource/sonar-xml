/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2018 SonarSource SA
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
package org.sonar.plugins.xml.newchecks;

import org.junit.Test;
import org.sonar.plugins.xml.newparser.checks.NewXmlVerifier;

public class XPathCheckTest {

  @Test
  public void test_nodes() throws Exception {
    NewXmlVerifier.verifyIssues("simple.xml", getCheck("//b"));
  }

  @Test
  public void test_file() throws Exception {
    NewXmlVerifier.verifyIssueOnFile("simple.xml", getCheck("boolean(a)"), "XPath issue message");
    NewXmlVerifier.verifyNoIssue("simple.xml", getCheck("not(boolean(a))"));
  }

  @Test(expected = IllegalStateException.class)
  public void test_invalid_xpath() throws Exception {
    NewXmlVerifier.verifyNoIssue("simple.xml", getCheck("boolean(a"));
  }

  @Test
  public void test_file_pattern() throws Exception {
    XPathCheck check = new XPathCheck();
    check.setExpression("//b");
    check.setMessage("XPath issue message");
    check.setFilePattern("**/FOOBAR/*.xml");

    NewXmlVerifier.verifyNoIssue("simple.xml", check);
  }

  @Test
  public void test_with_namespaces() throws Exception {
    NewXmlVerifier.verifyIssues("with_namespaces.xml", getCheck("//x:template"));
  }

  @Test
  public void test_with_default_namespace() throws Exception {
    NewXmlVerifier.verifyIssues("with_default_namespaces.xml", getCheck("//template"));
  }

  private static XPathCheck getCheck(String expression) {
    XPathCheck check = new XPathCheck();
    check.setExpression(expression);
    check.setMessage("XPath issue message");
    check.setFilePattern("**/XPathCheck/*.xml");

    return check;
  }

}
