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
package org.sonar.plugins.xml.parsers;

import com.google.common.base.Joiner;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SaxParserTest {

  @Test
  public void shouldParseComments() {
    String s = lines(
        "<!-- Foo -->",
        "<xml>",
        "<!-- Bar -->",
        "</xml>",
        "<!-- Baz -->");
    InputStream input = new ByteArrayInputStream(s.getBytes());
    Document doc = new SaxParser().parseDocument(input, false);

    Node node;

    node = doc.getChildNodes().item(0);
    assertThat(node.getNodeType(), is(Node.COMMENT_NODE));
    assertThat(node.getNodeValue(), is(" Foo "));
    assertThat(SaxParser.getLineNumber(node), is(1));

    node = doc.getChildNodes().item(1).getChildNodes().item(1);
    assertThat(node.getNodeType(), is(Node.COMMENT_NODE));
    assertThat(node.getNodeValue(), is(" Bar "));
    assertThat(SaxParser.getLineNumber(node), is(3));

    node = doc.getChildNodes().item(2);
    assertThat(node.getNodeType(), is(Node.COMMENT_NODE));
    assertThat(node.getNodeValue(), is(" Baz "));
    assertThat(SaxParser.getLineNumber(node), is(5));
  }

  private static String lines(String... lines) {
    return Joiner.on('\n').join(lines);
  }

}
