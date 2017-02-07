/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.highlighting;

import org.sonar.api.batch.sensor.highlighting.TypeOfText;

public class HighlightingData {

  private Integer startOffset;
  private Integer endOffset;
  private TypeOfText typeOfText;

  HighlightingData(Integer startOffset, Integer endOffset, TypeOfText typeOfText) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.typeOfText = typeOfText;
  }

  public Integer startOffset() {
    return startOffset;
  }

  public Integer endOffset() {
    return endOffset;
  }

  public TypeOfText highlightCode() {
    return typeOfText;
  }

}
