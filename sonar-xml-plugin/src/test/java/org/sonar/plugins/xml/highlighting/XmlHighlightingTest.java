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
package org.sonar.plugins.xml.highlighting;

import com.google.common.base.Strings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.xml.checks.XmlFile;
import org.sonar.plugins.xml.language.Xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.sonar.plugins.xml.compat.CompatibilityHelper.wrap;

public class XmlHighlightingTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test
  public void testCDATAWithTagsInside() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><![CDATA[<tag/><!-- Comment -->]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    // <tag
    assertData(highlightingData.get(0), 0, 4, TypeOfText.KEYWORD);
    // >
    assertData(highlightingData.get(1), 4, 5, TypeOfText.KEYWORD);

    // <![CDATA[
    assertData(highlightingData.get(2), 5, 14, TypeOfText.KEYWORD);
    // ]]>
    assertData(highlightingData.get(3), 36, 39, TypeOfText.KEYWORD);

    // </tag>
    assertData(highlightingData.get(4), 39, 45, TypeOfText.KEYWORD);
  }

  @Test
  public void testCDATAWithBracketInside() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><![CDATA[aa]>bb]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());

    // <![CDATA[
    assertData(highlightingData.get(2), 5, 14, TypeOfText.KEYWORD);
    // ]]>
    assertData(highlightingData.get(3), 20, 23, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightAutoclosingTagWithAttribute() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<input type='checkbox' />").getHighlightingData();
    assertEquals(5, highlightingData.size());
    // <input
    assertData(highlightingData.get(0), 0, 6, TypeOfText.KEYWORD);
    // type
    assertData(highlightingData.get(1), 7, 11, TypeOfText.CONSTANT);
    // 'checkbox'
    assertData(highlightingData.get(2), 12, 22, TypeOfText.STRING);
    // /
    assertData(highlightingData.get(4), 23, 24, TypeOfText.KEYWORD);
    // >
    assertData(highlightingData.get(3), 24, 25, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightTagWithDoubleQuoteAttribute() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag att=\"value ' with simple quote\"> </tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    // <tag
    assertData(highlightingData.get(0), 0, 4, TypeOfText.KEYWORD);
    // att
    assertData(highlightingData.get(1), 5, 8, TypeOfText.CONSTANT);
    // "value ' with simple quote"
    assertData(highlightingData.get(2), 9, 36, TypeOfText.STRING);
    // </tag>
    assertData(highlightingData.get(3), 36, 37, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightMultilineTagWithAttributes() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag att1='value1' \n att2\n = 'value2' att3=\n'value3' att4='multiline \n \" attribute'> </tag>").getHighlightingData();
    assertEquals(11, highlightingData.size());
    // <tag
    assertData(highlightingData.get(0), 0, 4, TypeOfText.KEYWORD);

    assertData(highlightingData.get(1), 5, 9, TypeOfText.CONSTANT);
    assertData(highlightingData.get(2), 10, 18, TypeOfText.STRING);
    assertData(highlightingData.get(3), 21, 27, TypeOfText.CONSTANT);
    assertData(highlightingData.get(4), 29, 37, TypeOfText.STRING);
    assertData(highlightingData.get(5), 38, 42, TypeOfText.CONSTANT);
    assertData(highlightingData.get(6), 44, 52, TypeOfText.STRING);
    assertData(highlightingData.get(7), 53, 57, TypeOfText.CONSTANT);
    assertData(highlightingData.get(8), 58, 83, TypeOfText.STRING);

    // >
    assertData(highlightingData.get(9), 83, 84, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightMultilineComments() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><!-- hello \n" +
      " world!! --></tag>").getHighlightingData();
    assertEquals(4, highlightingData.size());
    assertData(highlightingData.get(2), 5, 29, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  public void testWindowsLineEndingInComment() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><!-- hello \r\n" +
      " world!! --></tag>").getHighlightingData();
    assertEquals(4, highlightingData.size());
    assertData(highlightingData.get(2), 5, 30, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  public void testAttributeValueWithEqual() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<meta content=\"charset=UTF-8\" />").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 14, 29, TypeOfText.STRING);
  }

  @Test
  public void testHighlightCommentsAndOtherTag() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<!-- comment --><tag/>").getHighlightingData();
    assertEquals(4, highlightingData.size());
    assertData(highlightingData.get(0), 0, 16, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  public void testHighlightDoctype() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<!DOCTYPE foo> <tag/>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(0), 0, 9, TypeOfText.STRUCTURED_COMMENT);
    assertData(highlightingData.get(1), 13, 14, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  public void testCDATA() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag><![CDATA[foo]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 5, 14, TypeOfText.KEYWORD);
    assertData(highlightingData.get(3), 17, 20, TypeOfText.KEYWORD);
  }

  @Test
  public void testCDATAMultiline() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting(
      "<tag><![CDATA[foo\n" +
        "bar\n" +
        "]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 5, 14, TypeOfText.KEYWORD);
    assertData(highlightingData.get(3), 22, 25, TypeOfText.KEYWORD);
  }

  @Test
  public void testBigCDATA() throws Exception {
    String cdataContent = Strings.repeat("x", 100000);
    List<HighlightingData> highlightingData = new XMLHighlighting(
      "<tag><![CDATA[" + cdataContent + "]]></tag>").getHighlightingData();
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 5, 14, TypeOfText.KEYWORD);
    int expectedCDataEndOffset = 14 + cdataContent.length();
    assertData(highlightingData.get(3), expectedCDataEndOffset, expectedCDataEndOffset + 3, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightTag() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tr></tr>").getHighlightingData();
    assertEquals(3, highlightingData.size());
    // <tr
    assertData(highlightingData.get(0), 0, 3, TypeOfText.KEYWORD);
    // >
    assertData(highlightingData.get(1), 3, 4, TypeOfText.KEYWORD);
    // </tr>
    assertData(highlightingData.get(2), 4, 9, TypeOfText.KEYWORD);
  }

  @Test
  public void testEmptyElement() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<br/>").getHighlightingData();
    assertEquals(3, highlightingData.size());
    // <br
    assertData(highlightingData.get(0), 0, 3, TypeOfText.KEYWORD);
    // /
    assertData(highlightingData.get(2), 3, 4, TypeOfText.KEYWORD);
    // >
    assertData(highlightingData.get(1), 4, 5, TypeOfText.KEYWORD);
  }

  @Test
  public void testSpacesInside() throws Exception {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag > </tag >").getHighlightingData();
    assertEquals(3, highlightingData.size());
    assertData(highlightingData.get(0), 0, 4, TypeOfText.KEYWORD);
    assertData(highlightingData.get(1), 5, 6, TypeOfText.KEYWORD);
    assertData(highlightingData.get(2), 7, 14, TypeOfText.KEYWORD);

    highlightingData = new XMLHighlighting("<tag />").getHighlightingData();
    assertEquals(3, highlightingData.size());
    assertData(highlightingData.get(0), 0, 4, TypeOfText.KEYWORD);
    assertData(highlightingData.get(2), 5, 6, TypeOfText.KEYWORD);
    assertData(highlightingData.get(1), 6, 7, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightTagWithNamespace() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<tag xmlns:x='url'>\n<x:table>  </x:table></tag>").getHighlightingData();
    assertEquals(8, highlightingData.size());
    assertData(highlightingData.get(1), 5, 12, TypeOfText.CONSTANT);
    assertData(highlightingData.get(2), 13, 18, TypeOfText.STRING);
    assertData(highlightingData.get(4), 20, 28, TypeOfText.KEYWORD);
    assertData(highlightingData.get(5), 28, 29, TypeOfText.KEYWORD);
  }

  @Test
  public void testXMLHeader() throws XMLStreamException {
    List<HighlightingData> highlightingData = new XMLHighlighting("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>").getHighlightingData();
    assertEquals(9, highlightingData.size());
    // <?xml
    assertData(highlightingData.get(0), 0, 5, TypeOfText.KEYWORD);
    // ?>
    assertData(highlightingData.get(5), 37, 39, TypeOfText.KEYWORD);

    // version
    assertData(highlightingData.get(1), 6, 13, TypeOfText.CONSTANT);
    // "1.0"
    assertData(highlightingData.get(2), 14, 19, TypeOfText.STRING);
    // encoding
    assertData(highlightingData.get(3), 20, 28, TypeOfText.CONSTANT);
    // "UTF-8"
    assertData(highlightingData.get(4), 29, 36, TypeOfText.STRING);
  }

  @Test
  public void entity() throws Exception {
    assertThat(new XMLHighlighting("<a>&ouml;</a>").getHighlightingData()).isNotEmpty();
  }

  @Test
  public void testCharBeforeProlog() throws Exception {
    File file = tmpFolder.newFile("char_before_prolog.xml");
    FileUtils.write(file, "\n\n\n<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>");
    DefaultInputFile inputFile = new DefaultInputFile("module", "char_before_prolog.xml")
      .setModuleBaseDir(file.getParentFile().toPath())
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(StandardCharsets.UTF_8);
    DefaultFileSystem localFS = new DefaultFileSystem(new File(file.getParent()));
    localFS.add(inputFile).setWorkDir(tmpFolder.newFolder());

    XmlFile xmlFile = new XmlFile(wrap(inputFile), localFS);
    List<HighlightingData> highlightingData = new XMLHighlighting(xmlFile).getHighlightingData();
    assertEquals(9, highlightingData.size());
    // <?xml
    assertData(highlightingData.get(0), 3, 8, TypeOfText.KEYWORD);
    // ?>
    assertData(highlightingData.get(5), 40, 42, TypeOfText.KEYWORD);

    // version
    assertData(highlightingData.get(1), 9, 16, TypeOfText.CONSTANT);
    // "1.0"
    assertData(highlightingData.get(2), 17, 22, TypeOfText.STRING);
    // encoding
    assertData(highlightingData.get(3), 23, 31, TypeOfText.CONSTANT);
    // "UTF-8"
    assertData(highlightingData.get(4), 32, 39, TypeOfText.STRING);
  }

  @Test
  public void testBOM() throws Exception {
    HighlightingData firstHighlightingData = getFirstHighlightingData("bom.xml");
    // <beans
    assertData(firstHighlightingData, 0, 6, TypeOfText.KEYWORD);
  }

  @Test
  public void testBOMWithProlog() throws Exception {
    HighlightingData firstHighlightingData = getFirstHighlightingData("bomWithProlog.xml");
    // <?xml
    assertData(firstHighlightingData, 0, 5, TypeOfText.KEYWORD);
  }

  @Test
  public void testBOMWithCharBeforeProlog() throws Exception {
    HighlightingData firstHighlightingData = getFirstHighlightingData("bomCharBeforeProlog.xml");
    // <?xml
    // Below allows for carriage return and newline character usage differences between the Linux and Windows platforms
    assertTrue(firstHighlightingData.startOffset() == 1 || firstHighlightingData.startOffset() == 2);
    assertTrue(firstHighlightingData.endOffset() == 6 || firstHighlightingData.endOffset() == 7);
    assertTrue(TypeOfText.KEYWORD.equals(firstHighlightingData.highlightCode()));
  }

  private HighlightingData getFirstHighlightingData(String filename) throws IOException {
    File file = new File("src/test/resources/highlighting/" + filename);
    DefaultInputFile inputFile = new DefaultInputFile("modulekey", filename)
      .setModuleBaseDir(file.getParentFile().toPath())
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(StandardCharsets.UTF_8);
    DefaultFileSystem localFS = new DefaultFileSystem(new File(file.getParent()));
    localFS.setEncoding(StandardCharsets.UTF_8);
    localFS.add(inputFile).setWorkDir(tmpFolder.newFolder());

    XmlFile xmlFile = new XmlFile(wrap(inputFile), localFS);
    return new XMLHighlighting(xmlFile).getHighlightingData().get(0);
  }

  private void assertData(HighlightingData data, Integer start, Integer end, TypeOfText code) {
    assertEquals(start, data.startOffset());
    assertEquals(end, data.endOffset());
    assertEquals(code, data.highlightCode());
  }

  @Test
  public void should_parse_file_with_its_own_encoding() throws IOException, XMLStreamException {
    Charset fileSystemCharset = StandardCharsets.UTF_8;
    Charset fileCharset = StandardCharsets.UTF_16;

    Path moduleBaseDir = tmpFolder.newFolder().toPath();
    SensorContextTester context = SensorContextTester.create(moduleBaseDir);

    DefaultFileSystem fileSystem = new DefaultFileSystem(moduleBaseDir);
    fileSystem.setEncoding(fileSystemCharset);
    context.setFileSystem(fileSystem);

    String filename = "utf16.xml";
    try (BufferedWriter writer = Files.newBufferedWriter(moduleBaseDir.resolve(filename), fileCharset)) {
      writer.write("<?xml version=\"1.0\" encoding=\"utf-16\" standalone=\"yes\"?>\n");
      writer.write("<tag></tag>");
    }

    String modulekey = "modulekey";
    DefaultInputFile defaultInputFile = new DefaultInputFile(modulekey, filename)
      .setModuleBaseDir(moduleBaseDir)
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(fileCharset);
    defaultInputFile.initMetadata(new FileMetadata().readMetadata(defaultInputFile.file(), fileCharset));
    fileSystem.add(defaultInputFile);

    XmlFile xmlFile = new XmlFile(wrap(defaultInputFile), fileSystem);
    List<HighlightingData> highlightingData = new XMLHighlighting(xmlFile).getHighlightingData();
    assertThat(highlightingData).hasSize(11);
  }
}
