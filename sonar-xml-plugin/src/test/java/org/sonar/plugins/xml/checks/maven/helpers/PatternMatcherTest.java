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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PatternMatcherTest {

  @Test
  void should_match_patterns() {
    PatternMatcher matcher = new PatternMatcher("[a-z]*");
    assertThat(matcher.test("test")).isTrue();
    assertThat(matcher.test("012")).isFalse();
  }

  @Test
  void should_fail_on_invalid_regex() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
      () -> new PatternMatcher("*"));
    assertThat(e.getMessage()).startsWith("Unable to compile the regular expression '*', ");
  }

}
