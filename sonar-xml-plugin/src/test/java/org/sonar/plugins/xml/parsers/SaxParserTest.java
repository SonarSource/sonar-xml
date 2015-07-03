/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.xml.parsers;

import com.google.common.base.Joiner;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
    Document doc = new SaxParser().parseDocument(null, input, false);

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
