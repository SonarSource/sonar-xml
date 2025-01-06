/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml;

import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.w3c.dom.Element;

public class Utils {
  private Utils() {
    // utility class, forbidden constructor
  }

  public static String[] splitLines(String text) {
    return text.split("(\r)?\n|\r", -1);
  }

  /**
   * Check if element is self closing: &lt;foo ... /&gt;
   *
   * @param element element to check
   * @return true if element is self closing, false otherwise
   */
  public static boolean isSelfClosing(Element element) {
    XmlTextRange startLocation = XmlFile.startLocation(element);
    XmlTextRange endLocation = XmlFile.endLocation(element);
    return startLocation.getEndLine() == endLocation.getEndLine()
      && startLocation.getEndColumn() == endLocation.getEndColumn();
  }
}
