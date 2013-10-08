/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
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
package org.sonar.xml;

import org.junit.Test;
import org.sonar.colorizer.CodeColorizer;

import java.io.StringReader;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class XmlCodeColorizerFormatTest {

  private CodeColorizer codeColorizer = new CodeColorizer(XmlColorizer.createTokenizers());

  @Test
  public void testXMLHeader() {
    assertThat(
        highlight("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"),
        containsString("<span class=\"k\">&lt;?xml</span> <span class=\"c\">version</span>=<span class=\"s\">\"1.0\"</span> <span class=\"c\">encoding</span>=<span class=\"s\">\"UTF-8\"</span><span class=\"k\">?&gt;</span>"));
  }

  @Test
  public void testCDATA() {
    assertThat(highlight("<![CDATA[foo]]>"), containsString("<span class=\"k\">&lt;![CDATA[</span>foo<span class=\"k\">]]&gt;</span>"));
  }

  @Test
  public void testCDATAMultiline() {
    String cdataMultiline = highlight("<![CDATA[foo\nbar\n]]>");
    assertThat(cdataMultiline, containsString("<span class=\"k\">&lt;![CDATA[</span>foo"));
    assertThat(cdataMultiline, containsString("bar"));
    assertThat(cdataMultiline, containsString("<span class=\"k\">]]&gt;</span>"));
  }

  @Test
  public void testHighlightTag() {
    assertThat(highlight("</tr>"), containsString("<span class=\"k\">&lt;/tr&gt;</span>"));
    assertThat(highlight("<h3>"), containsString("<span class=\"k\">&lt;h3</span><span class=\"k\">&gt;</span>"));
    assertThat(highlight("<active-rule>"), containsString("<span class=\"k\">&lt;active-rule</span><span class=\"k\">&gt;</span>"));
    assertThat(highlight("</active-rule>"), containsString("<span class=\"k\">&lt;/active-rule&gt;</span>"));
  }

  @Test
  public void testHighlightTagWithNamespace() {
    assertThat(highlight("<namespace:table >"), containsString("<span class=\"k\">&lt;namespace:table</span> <span class=\"k\">&gt;</span>"));
  }

  @Test
  public void testHighlightTagWithSingleQuoteAttribute() {
    assertThat(highlight("<tag att='value \" with double quote'>"),
        containsString("<span class=\"k\">&lt;tag</span> <span class=\"c\">att</span>=<span class=\"s\">'value \" with double quote'</span><span class=\"k\">&gt;</span>"));
  }

  @Test
  public void testHighlightAutoclosingTagWithAttribute() {
    assertThat(highlight("<input type='checkbox' />"),
        containsString("<span class=\"k\">&lt;input</span> <span class=\"c\">type</span>=<span class=\"s\">'checkbox'</span> <span class=\"k\">/&gt;</span>"));
  }

  @Test
  public void testHighlightTagWithDoubleQuoteAttribute() {
    assertThat(highlight("<tag att=\"value ' with simple quote\">"),
        containsString("<span class=\"k\">&lt;tag</span> <span class=\"c\">att</span>=<span class=\"s\">\"value ' with simple quote\"</span><span class=\"k\">&gt;</span>"));
  }

  @Test
  public void testHighlightMultilineTagWithAttributes() {
    String multilinetag = highlight("<tag att1='value1' \n att2\n = 'value2' att3=\n'value3' att4='multiline \n \" attribute'>");
    assertThat(multilinetag,
        containsString("<span class=\"k\">&lt;tag</span> <span class=\"c\">att1</span>=<span class=\"s\">'value1'</span> "));
    assertThat(multilinetag, containsString(" <span class=\"c\">att2</span>"));
    assertThat(multilinetag, containsString(" = <span class=\"s\">'value2'</span> <span class=\"c\">att3</span>="));
    assertThat(multilinetag, containsString("<span class=\"s\">'value3'</span> <span class=\"c\">att4</span>=<span class=\"s\">'multiline </span>"));
    assertThat(multilinetag, containsString("<span class=\"s\">\" attribute'</span>"));
  }

  @Test
  public void testHighlightComments() {
    assertThat(highlight("<!-- hello world!! --> Foo"), containsString("<span class=\"j\">&lt;!-- hello world!! --&gt;</span> Foo"));
  }

  @Test
  public void testHighlightMultilineComments() {
    String commentMultiline = highlight("<!-- hello \n world!! --> Foo");
    assertThat(commentMultiline, containsString("<span class=\"j\">&lt;!-- hello </span>"));
    assertThat(commentMultiline, containsString("<span class=\"j\"> world!! --&gt;</span> Foo"));
  }

  @Test
  public void testHighlightCommentsAndOtherTag() {
    assertThat(
        highlight("<!-- hello world!! --><table size='45px'>"),
        containsString("<span class=\"j\">&lt;!-- hello world!! --&gt;</span><span class=\"k\">&lt;table</span> <span class=\"c\">size</span>=<span class=\"s\">'45px'</span><span class=\"k\">&gt;</span>"));
  }

  @Test
  public void testHighlightDoctype() {
    assertThat(highlight("<!DOCTYPE foo bar >"), containsString("<span class=\"j\">&lt;!DOCTYPE foo bar &gt;</span>"));
  }

  private String highlight(String webSourceCode) {
    return codeColorizer.toHtml(new StringReader(webSourceCode));
  }

}
