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
package org.sonar.plugins.xml.highlighting;

import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.fest.assertions.Assertions.assertThat;

public class XmlHighlightingTest {

  @Test
  public void testCDATAWithTagsInside() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><![CDATA[<tag/><!-- Comment -->]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    // <tag
    assertData(highlightingData.get(0), 0, 4, "k");
    // >
    assertData(highlightingData.get(1), 4, 5, "k");

    // <![CDATA[
    assertData(highlightingData.get(2), 5, 14, "k");
    // ]]>
    assertData(highlightingData.get(3), 36, 39, "k");

    // </tag>
    assertData(highlightingData.get(4), 39, 45, "k");
  }

  @Test
  public void testCDATAWithBracketInside() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><![CDATA[aa]>bb]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());

    // <![CDATA[
    assertData(highlightingData.get(2), 5, 14, "k");
    // ]]>
    assertData(highlightingData.get(3), 20, 23, "k");
  }

  @Test
  public void testHighlightAutoclosingTagWithAttribute() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<input type='checkbox' />").getHighlightingData();
    assertEquals(5, highlightingData.size());
    // <input
    assertData(highlightingData.get(0), 0, 6, "k");
    // type
    assertData(highlightingData.get(1), 7, 11, "c");
    // 'checkbox'
    assertData(highlightingData.get(2), 12, 22, "s");
    // /
    assertData(highlightingData.get(4), 23, 24, "k");
    // >
    assertData(highlightingData.get(3), 24, 25, "k");
  }

  @Test
  public void testHighlightTagWithDoubleQuoteAttribute() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag att=\"value ' with simple quote\"> </tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    // <tag
    assertData(highlightingData.get(0), 0, 4, "k");
    // att
    assertData(highlightingData.get(1), 5, 8, "c");
    // "value ' with simple quote"
    assertData(highlightingData.get(2), 9, 36, "s");
    // </tag>
    assertData(highlightingData.get(3), 36, 37, "k");
  }

  @Test
  public void testHighlightMultilineTagWithAttributes() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag att1='value1' \n att2\n = 'value2' att3=\n'value3' att4='multiline \n \" attribute'> </tag>").getHighlightingData();
    assertEquals(11, highlightingData.size());
    // <tag
    assertData(highlightingData.get(0), 0, 4, "k");

    assertData(highlightingData.get(1), 5, 9, "c");
    assertData(highlightingData.get(2), 10, 18, "s");
    assertData(highlightingData.get(3), 21, 27, "c");
    assertData(highlightingData.get(4), 29, 37, "s");
    assertData(highlightingData.get(5), 38, 42, "c");
    assertData(highlightingData.get(6), 44, 52, "s");
    assertData(highlightingData.get(7), 53, 57, "c");
    assertData(highlightingData.get(8), 58, 83, "s");

    // >
    assertData(highlightingData.get(9), 83, 84, "k");
  }

  @Test
  public void testHighlightMultilineComments() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><!-- hello \n" +
      " world!! --></tag>").getHighlightingData();
    assertEquals(4, highlightingData.size());
    assertData(highlightingData.get(2), 5, 29, "j");
  }

  @Test
  public void testAttributeValueWithEqual() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<meta content=\"charset=UTF-8\" />").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 14, 29, "s");
  }

  @Test
  public void testHighlightCommentsAndOtherTag() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<!-- comment --><tag/>").getHighlightingData();
    assertEquals(4, highlightingData.size());
    assertData(highlightingData.get(0), 0, 16, "j");
  }

  @Test
  public void testHighlightDoctype() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<!DOCTYPE foo> <tag/>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(0), 0, 9, "j");
    assertData(highlightingData.get(1), 13, 14, "j");
  }

  @Test
  public void testCDATA() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><![CDATA[foo]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 5, 14, "k");
    assertData(highlightingData.get(3), 17, 20, "k");
  }

  @Test
  public void testCDATAMultiline() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting(
      "<tag><![CDATA[foo\n" +
        "bar\n" +
        "]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 5, 14, "k");
    assertData(highlightingData.get(3), 22, 25, "k");
  }

  @Test
  public void testHighlightTag() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tr></tr>").getHighlightingData();
    assertEquals(3, highlightingData.size());
    // <tr
    assertData(highlightingData.get(0), 0, 3, "k");
    // >
    assertData(highlightingData.get(1), 3, 4, "k");
    // </tr>
    assertData(highlightingData.get(2), 4, 9, "k");
  }

  @Test
  public void testEmptyElement() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<br/>").getHighlightingData();
    assertEquals(3, highlightingData.size());
    // <br
    assertData(highlightingData.get(0), 0, 3, "k");
    // /
    assertData(highlightingData.get(2), 3, 4, "k");
    // >
    assertData(highlightingData.get(1), 4, 5, "k");
  }

  @Test
  public void testSpacesInside() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag > </tag >").getHighlightingData();
    assertEquals(3, highlightingData.size());
    assertData(highlightingData.get(0), 0, 4, "k");
    assertData(highlightingData.get(1), 5, 6, "k");
    assertData(highlightingData.get(2), 7, 14, "k");

    highlightingData = new XMLHighlighting("<tag />").getHighlightingData();
    assertEquals(3, highlightingData.size());
    assertData(highlightingData.get(0), 0, 4, "k");
    assertData(highlightingData.get(2), 5, 6, "k");
    assertData(highlightingData.get(1), 6, 7, "k");
  }

  @Test
  public void testHighlightTagWithNamespace() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag xmlns:x='url'>\n<x:table>  </x:table></tag>").getHighlightingData();
    assertEquals(8, highlightingData.size());
    assertData(highlightingData.get(1), 5, 12, "c");
    assertData(highlightingData.get(2), 13, 18, "s");
    assertData(highlightingData.get(4), 20, 28, "k");
    assertData(highlightingData.get(5), 28, 29, "k");
  }

  @Test
  public void testXMLHeader() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>").getHighlightingData();
    assertEquals(9, highlightingData.size());
    // <?xml
    assertData(highlightingData.get(0), 0, 5, "k");
    // ?>
    assertData(highlightingData.get(5), 37, 39, "k");

    // version
    assertData(highlightingData.get(1), 6, 13, "c");
    // "1.0"
    assertData(highlightingData.get(2), 14, 19, "s");
    // encoding
    assertData(highlightingData.get(3), 20, 28, "c");
    // "UTF-8"
    assertData(highlightingData.get(4), 29, 36, "s");
  }

  @Test
  public void entity() throws Exception {
    assertThat(new XMLHighlighting("<a>&ouml;</a>").getHighlightingData()).isNotEmpty();
  }

  private void assertData(HighlightingData data, Integer start, Integer end, String code) {
    assertEquals(start, data.startOffset());
    assertEquals(end, data.endOffset());
    assertEquals(code, data.highlightCode());
  }

}
