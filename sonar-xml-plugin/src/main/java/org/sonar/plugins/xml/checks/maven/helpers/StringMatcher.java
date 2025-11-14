/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
