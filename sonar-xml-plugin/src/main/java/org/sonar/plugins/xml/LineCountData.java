/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
