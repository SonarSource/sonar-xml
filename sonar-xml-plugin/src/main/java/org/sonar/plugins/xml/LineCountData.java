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
package org.sonar.plugins.xml;

import java.util.HashSet;
import java.util.Set;

public class LineCountData {

  private Integer linesNumber;
  private Set<Integer> linesOfCodeLines;
  private Set<Integer> effectiveCommentLine;

  public LineCountData(Integer linesNumber, Set<Integer> linesOfCodeLines, Set<Integer> effectiveCommentLine) {
    this.linesNumber = linesNumber;
    this.linesOfCodeLines = linesOfCodeLines;
    this.effectiveCommentLine = effectiveCommentLine;
  }

  public Integer linesNumber() {
    return linesNumber;
  }

  public Set<Integer> linesOfCodeLines() {
    return linesOfCodeLines;
  }

  public Set<Integer> effectiveCommentLines() {
    return effectiveCommentLine;
  }

  /**
   * Update lines measures if there is characters before prolog in xml file. As there are ignore
   * while analysing the file (to be able to process it), the lines need to be recomputed accordingly.
   */
  public void updateAccordingTo(int lineDelta) {
    if (lineDelta > 0) {
      Set<Integer> updatedLinesOfCodeLines = new HashSet<>();
      Set<Integer> updatedEffectiveCommentLines = new HashSet<>();

      for (int i = 0; i <= linesNumber; i++) {
        if (linesOfCodeLines.contains(i)) {
          updatedLinesOfCodeLines.add(i + lineDelta);
        }
        if (effectiveCommentLine.contains(i)) {
          updatedEffectiveCommentLines.add(i + lineDelta);
        }
      }

      this.linesNumber += lineDelta;
      this.linesOfCodeLines = updatedLinesOfCodeLines;
      this.effectiveCommentLine = updatedEffectiveCommentLines;
    }
  }

}
