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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.plugins.xml.checks.XmlFile;
import org.sonar.plugins.xml.language.Xml;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class XmlHighlightingTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

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
  public void testWindowsLineEndingInComment() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><!-- hello \r\n" +
      " world!! --></tag>").getHighlightingData();
    assertEquals(4, highlightingData.size());
    assertData(highlightingData.get(2), 5, 30, "j");
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
  public void testBigCDATA() throws Exception {
    String cdataContent = Strings.repeat("x", 100000);
    List<HighlightingData> highlightingData = new XMLHighlighting(
      "<tag><![CDATA[" + cdataContent + "]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 5, 14, "k");
    int expectedCDataEndOffset = 14 + cdataContent.length();
    assertData(highlightingData.get(3), expectedCDataEndOffset, expectedCDataEndOffset + 3, "k");
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

  @Test
  public void testCharBeforeProlog() throws Exception {
    File file = tmpFolder.newFile("char_before_prolog.xml");
    FileUtils.write(file, "\n\n\n<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>");
    DefaultInputFile inputFile = new DefaultInputFile("char_before_prolog.xml")
        .setLanguage(Xml.KEY)
        .setType(InputFile.Type.MAIN)
        .setAbsolutePath(file.getAbsolutePath());
    DefaultFileSystem localFS = new DefaultFileSystem(new File(file.getParent()));
    localFS.add(inputFile).setWorkDir(tmpFolder.newFolder());

    XmlFile xmlFile = new XmlFile(inputFile, localFS);
    List<HighlightingData> highlightingData = new XMLHighlighting(xmlFile, localFS.encoding()).getHighlightingData();
    assertEquals(9, highlightingData.size());
    // <?xml
    assertData(highlightingData.get(0), 3, 8, "k");
    // ?>
    assertData(highlightingData.get(5), 40, 42, "k");

    // version
    assertData(highlightingData.get(1), 9, 16, "c");
    // "1.0"
    assertData(highlightingData.get(2), 17, 22, "s");
    // encoding
    assertData(highlightingData.get(3), 23, 31, "c");
    // "UTF-8"
    assertData(highlightingData.get(4), 32, 39, "s");
  }

  @Test
  public void testBOM() throws Exception {
    HighlightingData firstHighlightingData = getFirstHighlightingData("bom.xml");
    // <beans
    assertData(firstHighlightingData, 0, 6, "k");
  }

  @Test
  public void testBOMWithProlog() throws Exception {
    HighlightingData firstHighlightingData = getFirstHighlightingData("bomWithProlog.xml");
    // <?xml
    assertData(firstHighlightingData, 0, 5, "k");
  }

  @Test
  public void testBOMWithCharBeforeProlog() throws Exception {
    HighlightingData firstHighlightingData = getFirstHighlightingData("bomCharBeforeProlog.xml");
    // <?xml
    assertData(firstHighlightingData, 1, 6, "k");
  }

  private HighlightingData getFirstHighlightingData(String filename) throws IOException {
    File file = new File("src/test/resources/highlighting/" + filename);
    DefaultInputFile inputFile = new DefaultInputFile(filename)
        .setLanguage(Xml.KEY)
        .setType(InputFile.Type.MAIN)
        .setAbsolutePath(file.getAbsolutePath());
    DefaultFileSystem localFS = new DefaultFileSystem(new File(file.getParent()));
    localFS.setEncoding(Charsets.UTF_8);
    localFS.add(inputFile).setWorkDir(tmpFolder.newFolder());

    XmlFile xmlFile = new XmlFile(inputFile, localFS);
    return new XMLHighlighting(xmlFile, localFS.encoding()).getHighlightingData().get(0);
  }

  private void assertData(HighlightingData data, Integer start, Integer end, String code) {
    assertEquals(start, data.startOffset());
    assertEquals(end, data.endOffset());
    assertEquals(code, data.highlightCode());
  }

}
