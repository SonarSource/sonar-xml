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
package org.sonar.plugins.xml.checks.maven.helpers;

import java.util.function.Predicate;
import javax.annotation.Nullable;

@FunctionalInterface
public interface StringMatcher extends Predicate<String> {

  static StringMatcher any() {
    return str -> true;
  }

  static boolean isBlank(@Nullable String str) {
    int length;
    if (str == null || (length = str.length()) == 0) {
      return true;
    }
    for (int i = 0; i < length; i++) {
      char ch = str.charAt(i);
      if (!Character.isWhitespace(ch)) {
        return false;
      }
    }
    return true;
  }

  static boolean isWildCard(String pattern) {
    return "*".equals(pattern);
  }
}
