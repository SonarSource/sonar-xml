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

public class XmlTextRange {
  int startLine;
  int startColumn;
  int endLine;
  int endColumn;

  public XmlTextRange(int startLine, int startColumn, int endLine, int endColumn) {
    if (startLine > endLine) {
      throw new IllegalArgumentException("Cannot have a start line after end line");
    }
    if (startLine == endLine && startColumn > endColumn) {
      throw new IllegalArgumentException("Cannot have a start column after end column when on same line");
    }
    if (startLine == endLine && startColumn == endColumn) {
      throw new IllegalArgumentException("Cannot have an empty range");
    }
    this.startLine = startLine;
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  public XmlTextRange(XmlLocation start, XmlLocation end, XmlLocation xmlStart) {
    this(start.computeSqLine(xmlStart), start.computeSqColumn(xmlStart), end.computeSqLine(xmlStart), end.computeSqColumn(xmlStart));
  }

  public XmlTextRange(XmlTextRange start, XmlLocation end, XmlLocation xmlStart) {
    this(start.startLine, start.startColumn, end.computeSqLine(xmlStart), end.computeSqColumn(xmlStart));
  }

  public XmlTextRange(XmlTextRange start, XmlTextRange end) {
    this(start.startLine, start.startColumn, end.endLine, end.endColumn);
  }

  public int getStartLine() {
    return startLine;
  }

  public int getStartColumn() {
    return startColumn;
  }

  public int getEndLine() {
    return endLine;
  }

  public int getEndColumn() {
    return endColumn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    XmlTextRange that = (XmlTextRange) o;

    return startLine == that.startLine
      && startColumn == that.startColumn
      && endLine == that.endLine
      && endColumn == that.endColumn;
  }

  @Override
  public int hashCode() {
    int result = startLine;
    result = 31 * result + startColumn;
    result = 31 * result + endLine;
    result = 31 * result + endColumn;
    return result;
  }

  @Override
  public String toString() {
    return "{" + startLine +
      ":" + startColumn +
      " - " + endLine +
      ":" + endColumn +
      '}';
  }
}
