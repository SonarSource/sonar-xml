/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
