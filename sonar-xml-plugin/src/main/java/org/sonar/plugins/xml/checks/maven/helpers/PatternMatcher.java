/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.xml.checks.maven.helpers;

import java.util.regex.Pattern;

public class PatternMatcher implements StringMatcher {

  private final Pattern pattern;

  public PatternMatcher(String regex) {
    this.pattern = compileRegex(regex);
  }

  @Override
  public boolean test(String value) {
    return !value.isEmpty() && pattern.matcher(value).matches();
  }

  private static Pattern compileRegex(String regex) {
    try {
      return Pattern.compile(regex, Pattern.DOTALL);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Unable to compile the regular expression '"+regex+"', " + e.getMessage());
    }
  }

}
