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

import org.sonar.api.utils.SonarException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Comment Counting in XML files
 *
 * @author Matthijs Galesloot
 */
public final class LineCountParser extends AbstractParser {

  private final CommentHandler commentHandler;

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

    private int getNumCommentLines() {
      return commentLines.size();
    }

    private int getNumEffectiveCommentLines() {
      return effectiveCommentLines.size();
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

  public int getCommentLineNumber() {
    return commentHandler.getNumCommentLines();
  }

  public int getEffectiveCommentLineNumber() {
    return commentHandler.getNumEffectiveCommentLines();
  }

  public LineCountParser(InputStream input) {
    SAXParser parser = newSaxParser(false);
    try {
      XMLReader xmlReader = parser.getXMLReader();
      commentHandler = new CommentHandler();
      xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", commentHandler);
      parser.parse(input, commentHandler);

    } catch (IOException e) {
      throw new SonarException(e);

    } catch (SAXException e) {
      throw new SonarException(e);

    }
  }

}
