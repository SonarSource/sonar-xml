/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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
package org.sonar.plugins.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonarsource.analyzer.commons.xml.XmlFile;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@EnableRuleMigrationSupport
class XmlHighlightingTest {

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  private DefaultFileSystem fileSystem;
  private SensorContextTester context;
  private XmlFile xmlFile;

  @BeforeEach
  void setUp() throws Exception {
    context = SensorContextTester.create(tmpFolder.getRoot());
    fileSystem = context.fileSystem();
  }

  @Test
  void testCDATAWithTagsInside() throws Exception {
    highlight("<tag><![CDATA[<tag/><!-- Comment -->]]></tag>");
    // <tag
    assertHighlighting(0, 4, TypeOfText.KEYWORD);
    // >
    assertHighlighting(4, 5, TypeOfText.KEYWORD);

    // <![CDATA[
    assertHighlighting(5, 14, TypeOfText.KEYWORD);
    // ]]>
    assertHighlighting(36, 39, TypeOfText.KEYWORD);

    // </tag>
    assertHighlighting(39, 45, TypeOfText.KEYWORD);
  }

  @Test
  void testCDATAWithBracketInside() throws Exception {
    highlight("<tag><![CDATA[aa]>bb]]></tag>");

    // <![CDATA[
    assertHighlighting(5, 14, TypeOfText.KEYWORD);
    // ]]>
    assertHighlighting(20, 23, TypeOfText.KEYWORD);
  }

  @Test
  void testHighlightSelfClosingTagWithAttribute() throws Exception {
    highlight("<input type='checkbox' />");
    // "<input"
    assertHighlighting(0, 6, TypeOfText.KEYWORD);
    // "type"
    assertHighlighting(7, 11, TypeOfText.CONSTANT);
    // 'checkbox'
    assertHighlighting(12, 22, TypeOfText.STRING);
    // " />"
    assertHighlighting(22, 25, TypeOfText.KEYWORD);
  }

  @Test
  void testHighlightTagWithDoubleQuoteAttribute() throws Exception {
    highlight("<tag att=\"value ' with simple quote\"> </tag>");
    // <tag
    assertHighlighting(0, 4, TypeOfText.KEYWORD);
    // att
    assertHighlighting(5, 8, TypeOfText.CONSTANT);
    // "value ' with simple quote"
    assertHighlighting(9, 36, TypeOfText.STRING);
    // >
    assertHighlighting(36, 37, TypeOfText.KEYWORD);
    // </tag>
    assertHighlighting(38, 44, TypeOfText.KEYWORD);
  }

  @Test
  void testHighlightMultilineTagWithAttributes() throws Exception {
    highlight(
      "<tag att1='value1' \n"
        + " att2\n"
        + " = 'value2' att3=\n"
        + "'value3' att4='multiline \n"
        + " \" attribute'> </tag>");
    // <tag
    assertHighlighting(1, 0, 1, 4, TypeOfText.KEYWORD);

    assertHighlighting(1, 5, 1, 9, TypeOfText.CONSTANT);
    assertHighlighting(1, 10, 1, 18, TypeOfText.STRING);
    assertHighlighting(2, 1, 3, 1, TypeOfText.CONSTANT);
    assertHighlighting(3, 3, 3, 11, TypeOfText.STRING);
    assertHighlighting(3, 12, 3, 16, TypeOfText.CONSTANT);
    assertHighlighting(4, 0, 4, 8, TypeOfText.STRING);
    assertHighlighting(4, 9, 4, 13, TypeOfText.CONSTANT);
    assertHighlighting(4, 14, 5, 13, TypeOfText.STRING);

    // >
    assertHighlighting(5, 13, 5, 14, TypeOfText.KEYWORD);
  }

  @Test
  void testHighlightMultilineComments() throws Exception {
    highlight(
      "<tag><!-- hello \n"
        + " world!! --></tag>");
    assertHighlighting(1, 5, 2, 12, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  void testWindowsLineEndingInComment() throws Exception {
    highlight(
      "<tag><!-- hello \r\n"
        + " world!! --></tag>");
    assertHighlighting(1, 5, 2, 12, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  void testAttributeValueWithEqual() throws Exception {
    highlight("<meta content=\"charset=UTF-8\" />");
    assertHighlighting(14, 29, TypeOfText.STRING);
  }

  @Test
  void testHighlightCommentsAndOtherTag() throws Exception {
    highlight("<!-- comment --><tag/>");
    assertHighlighting(0, 16, TypeOfText.STRUCTURED_COMMENT);
  }

  @Test
  void testHighlightDoctype() throws Exception {
    highlight("<!DOCTYPE foo> <tag/>");
    assertHighlighting(0, 14, TypeOfText.STRUCTURED_COMMENT);
    assertHighlighting(15, 19, TypeOfText.KEYWORD);
    assertHighlighting(19, 21, TypeOfText.KEYWORD);
  }

  @Test
  void testCDATA() throws Exception {
    highlight("<tag><![CDATA[foo]]></tag>");
    assertHighlighting(5, 14, TypeOfText.KEYWORD);
    assertHighlighting(17, 20, TypeOfText.KEYWORD);
  }

  @Test
  void testCDATAMultiline() throws Exception {
    highlight(
      "<tag><![CDATA[foo\n"
        + "bar\n"
        + "]]></tag>");
    assertHighlighting(1, 5, 1, 14, TypeOfText.KEYWORD);
    assertHighlighting(3, 0, 3, 3, TypeOfText.KEYWORD);
  }

  @Test
  void testBigCDATA() throws Exception {
    StringBuilder sb = new StringBuilder();
    int length = 100000;
    for (int i = 0; i < length; i++) {
      sb.append("a");
    }
    String cdataContent = sb.toString();
    highlight("<tag><![CDATA[" + cdataContent + "]]></tag>");
    assertHighlighting(5, 14, TypeOfText.KEYWORD);
    int expectedCDataEndOffset = 14 + cdataContent.length();
    assertHighlighting(expectedCDataEndOffset, expectedCDataEndOffset + 3, TypeOfText.KEYWORD);
  }

  @Test
  void testHighlightTag() throws Exception {
    highlight("<tr></tr>");
    // <tr
    assertHighlighting(0, 3, TypeOfText.KEYWORD);
    // >
    assertHighlighting(3, 4, TypeOfText.KEYWORD);
    // </tr>
    assertHighlighting(4, 9, TypeOfText.KEYWORD);
  }

  @Test
  void testEmptyElement() throws Exception {
    highlight("<br/>");
    // <br
    assertHighlighting(0, 3, TypeOfText.KEYWORD);
    // "/>"
    assertHighlighting(3, 5, TypeOfText.KEYWORD);
  }

  @Test
  void testSpacesInside() throws Exception {
    highlight("<tag > </tag >");
    assertHighlighting(0, 4, TypeOfText.KEYWORD);
    assertHighlighting(4, 6, TypeOfText.KEYWORD);
    assertHighlighting(7, 14, TypeOfText.KEYWORD);

  }

  @Test
  void testSpacesInsideSelfClosing() throws Exception {
    highlight("<tag />");
    assertHighlighting(0, 4, TypeOfText.KEYWORD);
    assertHighlighting(4, 7, TypeOfText.KEYWORD);
  }

  @Test
  void testHighlightTagWithNamespace() throws Exception {
    highlight(
      "<tag xmlns:x='url'>\n"
        + "<x:table>  </x:table></tag>");
    assertHighlighting(1, 5, 1, 12, TypeOfText.CONSTANT);
    assertHighlighting(1, 13, 1, 18, TypeOfText.STRING);
    assertHighlighting(2, 0, 2, 8, TypeOfText.KEYWORD);
    assertHighlighting(2, 8, 2, 9, TypeOfText.KEYWORD);
  }

  @Test
  void testHighlightingTagWithNameSpaceMultipleLine() throws Exception {
    highlight(
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
        + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
        // ...
        + "</project>");
    // xmlns:xsi
    assertHighlighting(2, 51, 2, 60, TypeOfText.CONSTANT);
    // "http://www.w3.org/2001/XMLSchema-instance"
    assertHighlighting(2, 61, 2, 104, TypeOfText.STRING);
    // xsi:schemaLocation
    assertHighlighting(3, 0, 3, 18, TypeOfText.CONSTANT);
    // "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"
    assertHighlighting(3, 19, 3, 98, TypeOfText.STRING);
  }

  @Test
  void testXMLHeader() throws Exception {
    highlight("<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>");
    // <?xml
    assertHighlighting(0, 5, TypeOfText.KEYWORD);
    // ?>
    assertHighlighting(37, 39, TypeOfText.KEYWORD);

    // version
    assertHighlighting(6, 13, TypeOfText.CONSTANT);
    // "1.0"
    assertHighlighting(14, 19, TypeOfText.STRING);
    // encoding
    assertHighlighting(20, 28, TypeOfText.CONSTANT);
    // "UTF-8"
    assertHighlighting(29, 36, TypeOfText.STRING);
  }

  @Test
  void testCharBeforeProlog() throws Exception {
    highlightFromFile("char_before_prolog.xml",
      "\n"
        + "\n"
        + "\n"
        + "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <tag/>");
    // <?xml
    assertHighlighting(4, 0, 4, 5, TypeOfText.KEYWORD);
    // ?>
    assertHighlighting(4, 37, 4, 39, TypeOfText.KEYWORD);

    // version
    assertHighlighting(4, 6, 4, 13, TypeOfText.CONSTANT);
    // "1.0"
    assertHighlighting(4, 14, 4, 19, TypeOfText.STRING);
    // encoding
    assertHighlighting(4, 20, 4, 28, TypeOfText.CONSTANT);
    // "UTF-8"
    assertHighlighting(4, 29, 4, 36, TypeOfText.STRING);
  }

  @Test
  void testBOM() throws Exception {
    highlightFromFile("bom.xml");
    // <beans
    assertHighlighting(1, 0, 1, 6, TypeOfText.KEYWORD);
  }

  @Test
  void testBOMWithProlog() throws Exception {
    highlightFromFile("bomWithProlog.xml");
    // <?xml
    assertHighlighting(1, 0, 1, 5, TypeOfText.KEYWORD);
  }

  @Test
  void testBOMWithCharBeforeProlog() throws Exception {
    highlightFromFile("bomCharBeforeProlog.xml");
    // <?xml
    assertHighlighting(2, 0, 2, 5, TypeOfText.KEYWORD);
  }

  @Test
  void should_parse_file_with_its_own_encoding() throws Exception {
    Charset fileSystemCharset = UTF_8;
    Charset fileCharset = StandardCharsets.UTF_16;

    Path moduleBaseDir = tmpFolder.newFolder().toPath();
    context = SensorContextTester.create(moduleBaseDir);

    fileSystem = new DefaultFileSystem(moduleBaseDir);
    fileSystem.setEncoding(fileSystemCharset);
    context.setFileSystem(fileSystem);

    String filename = "utf16.xml";
    Path file = moduleBaseDir.resolve(filename);
    try (BufferedWriter writer = Files.newBufferedWriter(file, fileCharset)) {
      writer.write("<?xml version=\"1.0\" encoding=\"" + fileCharset.name().toLowerCase() + "\" standalone=\"yes\"?>\n");
      writer.write("<tag></tag>");
    }

    DefaultInputFile defaultInputFile = TestInputFileBuilder.create("moduleKey", filename)
      .setModuleBaseDir(moduleBaseDir)
      .initMetadata(Files.lines(file, fileCharset).collect(Collectors.joining("\n")))
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(fileCharset)
      .build();
    fileSystem.add(defaultInputFile);

    highlight(defaultInputFile);

    // <?xml
    assertHighlighting(0, 5, TypeOfText.KEYWORD);
    // version
    assertHighlighting(6, 13, TypeOfText.CONSTANT);
    // "1.0"
    assertHighlighting(14, 19, TypeOfText.STRING);
  }

  private void highlightFromFile(String filename, String content) throws Exception {
    File file = tmpFolder.newFile(filename);
    FileUtils.write(file, content, UTF_8);
    highlight(file, filename);
  }

  private void highlightFromFile(String filename) throws Exception {
    File file = new File("src/test/resources/highlighting/" + filename);
    highlight(file, filename);
  }

  private void highlight(File file, String filename) throws Exception {
    DefaultInputFile inputFile = TestInputFileBuilder.create("module", filename)
      .setModuleBaseDir(file.getParentFile().toPath())
      .initMetadata(Files.lines(file.toPath()).collect(Collectors.joining("\n")))
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(UTF_8)
      .build();

    highlight(inputFile);
  }

  private void highlight(String content) throws Exception {
    DefaultInputFile inputFile = TestInputFileBuilder.create("module", "myFile")
      .initMetadata(content)
      .setContents(content)
      .setType(InputFile.Type.MAIN)
      .setLanguage(Xml.KEY)
      .setCharset(UTF_8)
      .build();

    highlight(inputFile);
  }

  private void highlight(InputFile inputFile) throws Exception {
    fileSystem.add(inputFile);
    xmlFile = XmlFile.create(inputFile);
    XmlHighlighting.highlight(context, xmlFile);
  }

  private void assertHighlighting(int startColumn, int endColumn, TypeOfText code) {
    assertHighlighting(1, startColumn, 1, endColumn, code);
  }

  private void assertHighlighting(int startLine, int startColumn, int endLine, int endColumn, TypeOfText code) {
    String componentKey = xmlFile.getInputFile().key();
    assertThat(context.highlightingTypeAt(componentKey, startLine, startColumn)).contains(code);
    // last char is not included
    assertThat(context.highlightingTypeAt(componentKey, endLine, endColumn - 1)).contains(code);
  }
}
