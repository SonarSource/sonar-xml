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
package org.sonar.plugins.xml.newparser;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

class XmlLocation {

  private final String content;
  // both 1-based
  private final int line;
  private final int column;
  private final int characterOffset;

  XmlLocation(String content) {
    // based on XML parser:
    // - lines start at 1
    // - columns start at at 1
    // - offset start at at 0
    this(content, 1, 1, 0);
  }

  public XmlLocation(String content, Location location) {
    this(content, location.getLineNumber(), location.getColumnNumber(), location.getCharacterOffset());
  }

  public XmlLocation(String content, int line, int column, int characterOfffset) {
    this.content = content;
    this.line = line;
    this.column = column;
    this.characterOffset = characterOfffset;
  }

  // 1-based
  public int line() {
    return line;
  }

  // 1-based
  public int column() {
    return column;
  }

  public XmlLocation shift(int nbChar) throws XMLStreamException {
    if (characterOffset + nbChar > content.length()) {
      throw new XMLStreamException("Cannot shift by " + nbChar + "characters");
    }
    XmlLocation res = this;
    for (int i = 0; i < nbChar; i++) {
      res = res.shift(res.readChar());
    }
    return res;
  }

  public XmlLocation moveBackward() throws XMLStreamException {
    if (column == 1) {
      throw new XMLStreamException("Cannot move backward from column 1");
    }
    return new XmlLocation(content, line, column - 1, characterOffset - 1);
  }

  public char readChar() {
    return content.charAt(characterOffset);
  }

  private XmlLocation shift(char c) {
    // what if old mac system with `\r` for new line
    if (c == '\n') {
      return new XmlLocation(content, line + 1, 1, characterOffset + 1);
    }
    return new XmlLocation(content, line, column + 1, characterOffset + 1);
  }

  public boolean startsWith(String prefix) {
    return content.startsWith(prefix, characterOffset);
  }

  public XmlLocation moveAfter(String substring) throws XMLStreamException {
    return moveBefore(substring).shift(substring.length());
  }

  public XmlLocation moveAfterClosingBracket() throws XMLStreamException {
    State state = State.START;

    int i = characterOffset + 1;
    while (i < content.length()) {
      char currentChar = content.charAt(i);

      state = statesMap.get(state).getOrDefault(currentChar, state);
      if (state == State.FINISH) {
        return shift(i - characterOffset + 1);
      }
      i++;
    }

    throw new IllegalStateException("Failed to find closing bracket '>'.");
  }

  public XmlLocation moveBefore(String substring) throws XMLStreamException {
    int index = content.indexOf(substring, characterOffset);
    if (index == -1) {
      throw new XMLStreamException("Cannot find " + substring + " in " + content.substring(characterOffset));
    }
    return shift(index - characterOffset);
  }

  public String textUntil(XmlLocation endLocation) {
    return content.substring(characterOffset, endLocation.characterOffset);
  }

  public XmlLocation moveAfterWhitespaces() throws XMLStreamException {
    XmlLocation res = this;
    while (Character.isWhitespace(res.readChar())) {
      res = res.shift(1);
    }
    return res;
  }

  public boolean has(String substring, XmlLocation max) throws XMLStreamException {
    XmlLocation location = this;
    while (location.characterOffset < max.characterOffset) {
      if (location.startsWith(substring)) {
        return true;
      }
      location = location.shift(1);
    }
    return false;
  }

  // zero-based
  public int computeSqColumn(XmlLocation xmlStartLocation) {
    int columnOffset = line == xmlStartLocation.line || line == 1 ? (xmlStartLocation.column - 1) : 0;
    // "-1" to make it zero-based
    return column + columnOffset - 1;
  }

  // one-based
  public int computeSqLine(XmlLocation xmlStartLocation) {
    return line + xmlStartLocation.line - 1;
  }

  private enum State {
    FINISH,
    START,
    INSIDE_NESTED_ELEMENT,
    INSIDE_SINGLE_QUOTE,
    INSIDE_DOUBLE_QUOTE,
    INSIDE_SINGLE_QUOTE_NESTED_ELEMENT,
    INSIDE_DOUBLE_QUOTE_NESTED_ELEMENT
  }

  private static Map<State, Map<Character, State>> statesMap = new EnumMap<>(State.class);

  static {
    Arrays.stream(State.values()).forEach(s -> statesMap.put(s, new HashMap<>()));

    statesMap.get(State.START).put('>', State.FINISH);
    statesMap.get(State.START).put('<', State.INSIDE_NESTED_ELEMENT);
    statesMap.get(State.START).put('\'', State.INSIDE_SINGLE_QUOTE);
    statesMap.get(State.START).put('"', State.INSIDE_DOUBLE_QUOTE);

    statesMap.get(State.INSIDE_NESTED_ELEMENT).put('>', State.START);
    statesMap.get(State.INSIDE_NESTED_ELEMENT).put('\'', State.INSIDE_SINGLE_QUOTE_NESTED_ELEMENT);
    statesMap.get(State.INSIDE_NESTED_ELEMENT).put('"', State.INSIDE_DOUBLE_QUOTE_NESTED_ELEMENT);

    statesMap.get(State.INSIDE_SINGLE_QUOTE).put('\'', State.START);
    statesMap.get(State.INSIDE_DOUBLE_QUOTE).put('"', State.START);

    statesMap.get(State.INSIDE_SINGLE_QUOTE_NESTED_ELEMENT).put('\'', State.INSIDE_NESTED_ELEMENT);
    statesMap.get(State.INSIDE_DOUBLE_QUOTE_NESTED_ELEMENT).put('"', State.INSIDE_NESTED_ELEMENT);
  }
}
