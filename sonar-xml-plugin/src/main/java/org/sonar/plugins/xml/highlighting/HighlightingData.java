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

import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;

public class HighlightingData {

  private final TypeOfText typeOfText;

  private int startLine;
  private int startColumnOffset;
  private int endLine;
  private int endColumnOffset;

  public HighlightingData(int startLine, int startColumnIndex, int endLine, int endColumnIndex, TypeOfText typeOfText) {
    this.startLine = startLine;
    this.startColumnOffset = startColumnIndex - 1;
    this.endLine = endLine;
    this.endColumnOffset = endColumnIndex - 1;
    this.typeOfText = typeOfText;
  }

  public int startLine() {
    return startLine;
  }

  public int startColumn() {
    return startColumnOffset;
  }

  public int endLine() {
    return endLine;
  }

  public int endColumn() {
    return endColumnOffset;
  }

  public TypeOfText highlightCode() {
    return typeOfText;
  }

  public void highlight(NewHighlighting highlighting) {
    highlighting.highlight(startLine, startColumnOffset, endLine, endColumnOffset, typeOfText);
  }

}
