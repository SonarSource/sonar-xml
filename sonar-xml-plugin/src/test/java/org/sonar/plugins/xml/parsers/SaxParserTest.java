/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
