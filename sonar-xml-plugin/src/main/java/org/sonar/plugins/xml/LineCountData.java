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
