/*
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
 * along with this program; if not, see https://www.sonarsource.com/legal/
 */
package org.sonar.plugins.xml.checks.maven.helpers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringMatcherTest {

  @Test
  void matcher_always_matching_always_match() {
    StringMatcher matcher = StringMatcher.any();
    assertThat(matcher.test(null)).isTrue();
    assertThat(matcher.test("test")).isTrue();
  }

  @Test
  void test_isBlank() {
    assertThat(StringMatcher.isBlank(null)).isTrue();
    assertThat(StringMatcher.isBlank("")).isTrue();
    assertThat(StringMatcher.isBlank(" ")).isTrue();
    assertThat(StringMatcher.isBlank("               ")).isTrue();
    assertThat(StringMatcher.isBlank("               \t\t\t")).isTrue();
    assertThat(StringMatcher.isBlank("abc")).isFalse();
  }
}
