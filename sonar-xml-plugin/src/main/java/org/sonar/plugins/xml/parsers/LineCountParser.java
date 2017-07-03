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
package org.sonar.plugins.xml.parsers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.SAXParser;
import org.apache.commons.lang.StringUtils;
import org.sonar.plugins.xml.LineCountData;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Counting comment lines, blank lines in XML files
 *
 * @author Matthijs Galesloot
 */
public final class LineCountParser extends AbstractParser {

  private CommentHandler commentHandler;
  private int linesNumber;
  private Set<Integer> linesOfCodeLines;
  private LineCountData data;

  public LineCountParser(String contents, Charset charset) throws IOException, SAXException {
    processCommentLines(contents, charset);
    processBlankLines(contents);
    this.data = new LineCountData(
      linesNumber,
      linesOfCodeLines,
      new HashSet<>(commentHandler.effectiveCommentLines));
  }

  private void processCommentLines(String contents, Charset charset) throws SAXException, IOException {
    SAXParser parser = newSaxParser(false);
    XMLReader xmlReader = parser.getXMLReader();
    commentHandler = new CommentHandler();
    xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", commentHandler);
    parser.parse(new ByteArrayInputStream(contents.getBytes(charset)), commentHandler);
  }

  private void processBlankLines(String contents) {
    Set<Integer> blankLines = new HashSet<>();
    String lineSeparatorRegexp = "(?:\r)?\n|\r";

    int currentLine = 0;

    for (String line : contents.split(lineSeparatorRegexp, -1)) {
      currentLine++;

      if (StringUtils.isBlank(line)) {
        blankLines.add(currentLine);
      }
    }

    linesNumber = currentLine;

    linesOfCodeLines = new HashSet<>();
    for (int line = 1; line <= linesNumber; line++) {
      if (!blankLines.contains(line) && !commentHandler.commentLines.contains(line)) {
        linesOfCodeLines.add(line);
      }
    }
  }

  public LineCountData getLineCountData() {
    return data;
  }

  private static class CommentHandler extends DefaultHandler implements LexicalHandler {
    private Deque<Integer> commentLines = new ArrayDeque<>();

    private Deque<Integer> effectiveCommentLines = new ArrayDeque<>();

    private int lastCodeLine = 0;

    private Locator locator;

    private void registerLineOfCode() {
      lastCodeLine = locator.getLineNumber();
      if (lastEffectiveCommentLine() == lastCodeLine) {
        effectiveCommentLines.pop();
      }
      if (lastCommentLine() == lastCodeLine) {
        commentLines.pop();
      }
    }

    private int lastEffectiveCommentLine() {
      return effectiveCommentLines.isEmpty() ? 0 : effectiveCommentLines.peek();
    }

    private int lastCommentLine() {
      return commentLines.isEmpty() ? 0 : commentLines.peek();
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
      String comment = new String(ch).substring(start, start + length);
      String[] lines = comment.split("\\n", -1);

      int currentLine = locator.getLineNumber() - lines.length + 1;

      for (String line : lines) {
        if (lastCommentLine() < currentLine && lastCodeLine < currentLine) {
          commentLines.push(currentLine);
        }

        String commentLine = line.trim();

        if (!commentLine.isEmpty() && lastEffectiveCommentLine() < currentLine && lastCodeLine < currentLine) {
          effectiveCommentLines.push(currentLine);
        }

        currentLine++;
      }
    }

    @Override
    public void endCDATA() throws SAXException {
      registerLineOfCode();
    }

    @Override
    public void endDTD() throws SAXException {
      registerLineOfCode();
    }

    @Override
    public void endEntity(String name) throws SAXException {
      registerLineOfCode();
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      if (e.getLocalizedMessage().contains(UnrecoverableParseError.FAILUREMESSAGE)) {
        throw new UnrecoverableParseError(e);
      }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    @Override
    public void startCDATA() throws SAXException {
      registerLineOfCode();
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
      registerLineOfCode();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      registerLineOfCode();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      registerLineOfCode();
    }

    @Override
    public void startEntity(String name) throws SAXException {
      registerLineOfCode();
    }

  }
}
