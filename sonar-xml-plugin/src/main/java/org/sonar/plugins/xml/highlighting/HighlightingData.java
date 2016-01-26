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

  

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((endOffset == null) ? 0 : endOffset.hashCode());
    result = prime * result + ((highlightCode == null) ? 0 : highlightCode.hashCode());
    result = prime * result + ((startOffset == null) ? 0 : startOffset.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    HighlightingData other = (HighlightingData) obj;
    if (endOffset == null) {
      if (other.endOffset != null)
        return false;
    } else if (!endOffset.equals(other.endOffset))
      return false;
    if (highlightCode == null) {
      if (other.highlightCode != null)
        return false;
    } else if (!highlightCode.equals(other.highlightCode))
      return false;
    if (startOffset == null) {
      if (other.startOffset != null)
        return false;
    } else if (!startOffset.equals(other.startOffset))
      return false;
    return true;
  }

}
