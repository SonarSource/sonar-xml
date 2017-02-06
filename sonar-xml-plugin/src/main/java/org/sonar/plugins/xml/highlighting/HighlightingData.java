/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.highlighting;

public class HighlightingData {

  private Integer startOffset;
  private Integer endOffset;
  private String highlightCode;

  public HighlightingData(Integer startOffset, Integer endOffset, String highlightCode) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.highlightCode = highlightCode;
  }

  public Integer startOffset() {
    return startOffset;
  }

  public Integer endOffset() {
    return endOffset;
  }

  public String highlightCode() {
    return highlightCode;
  }

}
