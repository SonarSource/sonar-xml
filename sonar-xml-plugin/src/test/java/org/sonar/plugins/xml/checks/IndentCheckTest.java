/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import java.io.IOException;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author Matthijs Galesloot
 */
public class IndentCheckTest extends AbstractCheckTester {

  @Test
  public void test_no_issues_on_comment() throws Exception {
    List<XmlIssue> issues = getIssues(
      "<root>\n" +
      "<!-- comment -->\n" +
      "</root>"
    );

    assertEquals(0, issues.size());
  }

  @Test
  public void test_one_issue_per_block() throws Exception {
    List<XmlIssue> issues = getIssues(
      "<root>\n" +
      "<tag>\n" +
      "<nested>value</nested>\n" +
      "</tag>\n" +
      "</root>"
    );

    assertEquals(1, issues.size());
  }

  @Test
  public void test_one_issue_per_line() throws Exception {
    List<XmlIssue> issues = getIssues(
      "<root>\n" +
      "<tag>value</tag> <tag>value</tag>\n" +
      "</root>"
    );

    assertEquals(1, issues.size());
  }

  @Test
  public void check_first_level_indent() throws IOException {
    List<XmlIssue> issues = getIssues(
      "<html>\n" +
      "<body><br>hello</br></body>\n" +
      "</html>"
    );

    assertEquals(1, issues.size());
    assertEquals("Make this line start at column 3.", issues.get(0).getMessage());
    assertThat(issues.get(0).getLine()).isEqualTo(2);
  }

  @Test
  public void check_second_level_indent() throws IOException {
    List<XmlIssue> issues = getIssues(
      "<html>\n" +
      "  <body>\n" +
      " <tag/>\n" +
      "  </body>\n" +
      "</html>"
    );

    assertEquals(1, issues.size());
    assertEquals("Make this line start at column 5.", issues.get(0).getMessage());
    assertThat(issues.get(0).getLine()).isEqualTo(3);
  }

  @Test
  public void check_tabs_indent() throws IOException {
    List<XmlIssue> issues = getIssues(
      "<html>\n" +
        "\t<body>\n" +
        "\t\t<br>hello</br>\n" +
        "\t</body>\n" +
        "</html>"
    );

    assertEquals(0, issues.size());
  }

  @Test
  public void non_whitespace_characters() throws Exception {
    List<XmlIssue> issues = getIssues(
      "<html>\n" +
        "  <body>\n" +
        "     xx<tag/>\n" +
        "  </body>\n" +
        "</html>");

    assertEquals(1, issues.size());
    assertThat(issues.get(0).getLine()).isEqualTo(3);
  }

  @Test
  public void comment_before_tag() throws Exception {
    List<XmlIssue> issues = getIssues(
      "<html>\n" +
        "  <body>\n" +
        "     <!--comment--><tag/>\n" +
        "  </body>\n" +
        "</html>");

    assertEquals(1, issues.size());
    assertThat(issues.get(0).getLine()).isEqualTo(3);
  }

  private List<XmlIssue> getIssues(String content) throws IOException {
    XmlSourceCode sourceCode = parseAndCheck(
      createTempFile(content),
      new IndentCheck());
    return sourceCode.getXmlIssues();
  }

}
