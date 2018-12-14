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
package org.sonar.plugins.xml.highlighting;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.newparser.NewXmlFile;
import org.sonar.plugins.xml.newparser.XmlTextRange;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class XmlHighlightingTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Test
  public void testCDATAWithTagsInside() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<tag><![CDATA[<tag/><!-- Comment -->]]></tag>");
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
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<tag><![CDATA[aa]>bb]]></tag>");
    assertEquals(5, highlightingData.size());

    // <![CDATA[
    assertData(highlightingData.get(2), 5, 14, TypeOfText.KEYWORD);
    // ]]>
    assertData(highlightingData.get(3), 20, 23, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightSelfClosingTagWithAttribute() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<input type='checkbox' />");
    assertEquals(4, highlightingData.size());
    // "<input"
    assertData(highlightingData.get(0), 0, 6, TypeOfText.KEYWORD);
    // "type"
    assertData(highlightingData.get(1), 7, 11, TypeOfText.CONSTANT);
    // 'checkbox'
    assertData(highlightingData.get(2), 12, 22, TypeOfText.STRING);
    // " />"
    assertData(highlightingData.get(3), 22, 25, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightTagWithDoubleQuoteAttribute() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<tag att=\"value ' with simple quote\"> </tag>");
    assertEquals(5, highlightingData.size());
    // <tag
    assertData(highlightingData.get(0), 0, 4, TypeOfText.KEYWORD);
    // att
    assertData(highlightingData.get(1), 5, 8, TypeOfText.CONSTANT);
    // "value ' with simple quote"
    assertData(highlightingData.get(2), 9, 36, TypeOfText.STRING);
    // >
    assertData(highlightingData.get(3), 36, 37, TypeOfText.KEYWORD);
    // </tag>
    assertData(highlightingData.get(4), 38, 44, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightMultilineTagWithAttributes() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent(
      "<tag att1='value1' \n"
        + " att2\n"
        + " = 'value2' att3=\n"
        + "'value3' att4='multiline \n"
        + " \" attribute'> </tag>");
    assertEquals(11, highlightingData.size());
    // <tag
    assertData(highlightingData.get(0), 1, 0, 1, 4, TypeOfText.KEYWORD);

    assertData(highlightingData.get(1), 1, 5, 1, 9, TypeOfText.CONSTANT);
    assertData(highlightingData.get(2), 1, 10, 1, 18, TypeOfText.STRING);
    assertData(highlightingData.get(3), 2, 1, 3, 1, TypeOfText.CONSTANT);
    assertData(highlightingData.get(4), 3, 3, 3, 11, TypeOfText.STRING);
    assertData(highlightingData.get(5), 3, 12, 3, 16, TypeOfText.CONSTANT);
    assertData(highlightingData.get(6), 4, 0, 4, 8, TypeOfText.STRING);
    assertData(highlightingData.get(7), 4, 9, 4, 13, TypeOfText.CONSTANT);
    assertData(highlightingData.get(8), 4, 14, 5, 13, TypeOfText.STRING);

    // >
    assertData(highlightingData.get(9), 5, 13, 5, 14, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightMultilineComments() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent(
      "<tag><!-- hello \n"
        + " world!! --></tag>");
    assertEquals(4, highlightingData.size());
    assertData(highlightingData.get(2), 1, 5, 2, 12, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  public void testWindowsLineEndingInComment() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent(
      "<tag><!-- hello \r\n"
        + " world!! --></tag>");
    assertEquals(4, highlightingData.size());
    assertData(highlightingData.get(2), 1, 5, 2, 12, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  public void testAttributeValueWithEqual() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<meta content=\"charset=UTF-8\" />");
    assertEquals(4, highlightingData.size());
    assertData(highlightingData.get(2), 14, 29, TypeOfText.STRING);
  }

  @Test
  public void testHighlightCommentsAndOtherTag() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<!-- comment --><tag/>");
    assertEquals(3, highlightingData.size());
    assertData(highlightingData.get(0), 0, 16, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  public void testHighlightDoctype() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<!DOCTYPE foo> <tag/>");
    assertEquals(3, highlightingData.size());
    assertData(highlightingData.get(0), 0, 14, TypeOfText.STRUCTURED_COMMENT);
    assertData(highlightingData.get(1), 15, 19, TypeOfText.KEYWORD);
    assertData(highlightingData.get(2), 19, 21, TypeOfText.KEYWORD);
  }

  @Test
  public void testCDATA() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<tag><![CDATA[foo]]></tag>");
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 5, 14, TypeOfText.KEYWORD);
    assertData(highlightingData.get(3), 17, 20, TypeOfText.KEYWORD);
  }

  @Test
  public void testCDATAMultiline() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent(
      "<tag><![CDATA[foo\n"
        + "bar\n"
        + "]]></tag>");
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 1, 5, 1, 14, TypeOfText.KEYWORD);
    assertData(highlightingData.get(3), 3, 0, 3, 3, TypeOfText.KEYWORD);
  }

  @Test
  public void testBigCDATA() throws Exception {
    StringBuilder sb = new StringBuilder();
    int length = 100000;
    for (int i = 0; i < length; i++) {
      sb.append("a");
    }
    String cdataContent = sb.toString();
    List<HighlightingData> highlightingData = getHighlightingDataFromContent(
      "<tag><![CDATA[" + cdataContent + "]]></tag>");
    assertEquals(5, highlightingData.size());
    assertData(highlightingData.get(2), 5, 14, TypeOfText.KEYWORD);
    int expectedCDataEndOffset = 14 + cdataContent.length();
    assertData(highlightingData.get(3), expectedCDataEndOffset, expectedCDataEndOffset + 3, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightTag() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<tr></tr>");
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
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<br/>");
    assertEquals(2, highlightingData.size());
    // <br
    assertData(highlightingData.get(0), 0, 3, TypeOfText.KEYWORD);
    // "/>"
    assertData(highlightingData.get(1), 3, 5, TypeOfText.KEYWORD);
  }

  @Test
  public void testSpacesInside() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<tag > </tag >");
    assertEquals(3, highlightingData.size());
    assertData(highlightingData.get(0), 0, 4, TypeOfText.KEYWORD);
    assertData(highlightingData.get(1), 4, 6, TypeOfText.KEYWORD);
    assertData(highlightingData.get(2), 7, 14, TypeOfText.KEYWORD);

    highlightingData = getHighlightingDataFromContent("<tag />");
    assertEquals(2, highlightingData.size());
    assertData(highlightingData.get(0), 0, 4, TypeOfText.KEYWORD);
    assertData(highlightingData.get(1), 4, 7, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightTagWithNamespace() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent(
      "<tag xmlns:x='url'>\n"
        + "<x:table>  </x:table></tag>");
    assertEquals(8, highlightingData.size());
    assertData(highlightingData.get(1), 1, 5, 1, 12, TypeOfText.CONSTANT);
    assertData(highlightingData.get(2), 1, 13, 1, 18, TypeOfText.STRING);
    assertData(highlightingData.get(4), 2, 0, 2, 8, TypeOfText.KEYWORD);
    assertData(highlightingData.get(5), 2, 8, 2, 9, TypeOfText.KEYWORD);
  }

  @Test
  public void testHighlightingTagWithNameSpaceMultipleLine() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent(
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        // ...
        + "</project>");
    assertEquals(15, highlightingData.size());
    // xmlns:xsi
    assertData(highlightingData.get(9), 2, 51, 2, 60, TypeOfText.CONSTANT);
    // "http://www.w3.org/2001/XMLSchema-instance"
    assertData(highlightingData.get(10), 2, 61, 2, 104, TypeOfText.STRING);
    // xsi:schemaLocation
    assertData(highlightingData.get(11), 3, 0, 3, 18, TypeOfText.CONSTANT);
    // "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
    assertData(highlightingData.get(12), 3, 19, 3, 98, TypeOfText.STRING);
  }

  @Test
  public void testXMLHeader() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromContent("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>");
    assertEquals(8, highlightingData.size());
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
  public void testCharBeforeProlog() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromNewFile("char_before_prolog.xml",
      "\n"
        + "\n"
        + "\n"
        + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>");
    assertEquals(8, highlightingData.size());
    // <?xml
    assertData(highlightingData.get(0), 4, 0, 4, 5, TypeOfText.KEYWORD);
    // ?>
    assertData(highlightingData.get(5), 4, 37, 4, 39, TypeOfText.KEYWORD);

    // version
    assertData(highlightingData.get(1), 4, 6, 4, 13, TypeOfText.CONSTANT);
    // "1.0"
    assertData(highlightingData.get(2), 4, 14, 4, 19, TypeOfText.STRING);
    // encoding
    assertData(highlightingData.get(3), 4, 20, 4, 28, TypeOfText.CONSTANT);
    // "UTF-8"
    assertData(highlightingData.get(4), 4, 29, 4, 36, TypeOfText.STRING);
  }

  @Test
  public void testBOM() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromFile("bom.xml");
    HighlightingData firstHighlightingData = highlightingData.get(0);
    // <beans
    assertData(firstHighlightingData, 1, 0, 1, 6, TypeOfText.KEYWORD);
  }

  @Test
  public void testBOMWithProlog() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromFile("bomWithProlog.xml");
    HighlightingData firstHighlightingData = highlightingData.get(0);
    // <?xml
    assertData(firstHighlightingData, 1, 0, 1, 5, TypeOfText.KEYWORD);
  }

  @Test
  public void testBOMWithCharBeforeProlog() throws Exception {
    List<HighlightingData> highlightingData = getHighlightingDataFromFile("bomCharBeforeProlog.xml");
    HighlightingData firstHighlightingData = highlightingData.get(0);
    // <?xml
    assertData(firstHighlightingData, 2, 0, 2, 5, TypeOfText.KEYWORD);
  }

  @Test
  public void should_parse_file_with_its_own_encoding() throws Exception {
    Charset fileSystemCharset = UTF_8;
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

    DefaultInputFile defaultInputFile = TestInputFileBuilder.create("moduleKey", filename)
      .setModuleBaseDir(moduleBaseDir)
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(fileCharset)
      .build();
    fileSystem.add(defaultInputFile);

    List<HighlightingData> highlightingData = getHighlightingData(defaultInputFile);
    assertThat(highlightingData).hasSize(11);
  }

  private List<HighlightingData> getHighlightingDataFromNewFile(String filename, String content) throws Exception {
    File file = tmpFolder.newFile(filename);
    FileUtils.write(file, content, UTF_8);
    return getHighlightingData(file, filename);
  }

  private List<HighlightingData> getHighlightingDataFromFile(String filename) throws Exception {
    File file = new File("src/test/resources/highlighting/" + filename);
    return getHighlightingData(file, filename);
  }

  private List<HighlightingData> getHighlightingData(File file, String filename) throws Exception {
    DefaultInputFile inputFile = TestInputFileBuilder.create("module", filename)
      .setModuleBaseDir(file.getParentFile().toPath())
      .initMetadata(Files.lines(file.toPath()).collect(Collectors.joining()))
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(UTF_8)
      .build();

    return getHighlightingData(inputFile);
  }

  private List<HighlightingData> getHighlightingDataFromContent(String content) throws Exception {
    DefaultInputFile inputFile = TestInputFileBuilder.create("module", "myFile")
      .initMetadata(content)
      .setContents(content)
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(UTF_8)
      .build();

    return getHighlightingData(inputFile);
  }

  private List<HighlightingData> getHighlightingData(InputFile inputFile) throws Exception {
    NewXmlFile newXmlFile = NewXmlFile.create(inputFile);
    return XMLHighlighting.highlight(newXmlFile);
  }

  private void assertData(HighlightingData data, int startColumn, int endColumn, TypeOfText code) {
    assertData(data, 1, startColumn, 1, endColumn, code);
  }

  private void assertData(HighlightingData data, int startLine, int startColumn, int endLine, int endColumn, TypeOfText code) {
    XmlTextRange textRange = data.getTextRange();
    assertThat(textRange.getStartLine()).as("start line").isEqualTo(startLine);
    assertThat(textRange.getStartColumn()).as("start column").isEqualTo(startColumn);
    assertThat(textRange.getEndLine()).as("end line").isEqualTo(endLine);
    assertThat(textRange.getEndColumn()).as("end column").isEqualTo(endColumn);
    assertThat(data.highlightCode()).as("Type of text").isEqualTo(code);
  }
}
