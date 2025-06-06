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
package org.sonar.plugins.xml.checks.maven.helpers;

import javax.annotation.Nullable;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RangedVersionMatcherTest {

  private RangedVersionMatcher matcher;

  @Test
  void no_version_never_match() {
    matcher = new RangedVersionMatcher("1.2", "1.5.6");
    assertNotMatch(null);
    assertNotMatch("");
  }

  @Test
  void invalid_version_never_match() {
    matcher = new RangedVersionMatcher("1.2", "1.5.6");
    assertNotMatch("invalid.0");
    assertNotMatch("1.invalid");
  }

  @Test
  void fail_with_double_wildcard() {
    assertThrows(IllegalArgumentException.class,
      () -> new RangedVersionMatcher("*", "*"));
  }

  @Test
  void fail_with_invalid_version() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
      () -> new RangedVersionMatcher("1.2.3", "invalid"));
    assertThat(e.getMessage())
      .isEqualTo("Invalid version range upper bound  'invalid'." +
        " Unsupported version format 'invalid'." +
        " The version does not match expected pattern: '<major version>.<minor version>.<incremental version>'");
  }

  @Test
  void version_after_range_never_match() {
    matcher = new RangedVersionMatcher("1.2", "1.5.6");
    assertNotMatch("1.5.7-SNAPSHOT");
    assertNotMatch("1.5.7");
    assertNotMatch("1.6");
    assertNotMatch("2");
  }

  @Test
  void version_before_range_never_match() {
    matcher = new RangedVersionMatcher("1.2", "1.5.6");
    assertNotMatch("1.1.9-SNAPSHOT");
    assertNotMatch("1.1.9");
    assertNotMatch("1.1");
    assertNotMatch("1");
    assertNotMatch("0");
  }

  @Test
  void version_in_range_always_match() {
    matcher = new RangedVersionMatcher("1.2", "1.5.6");
    assertMatches("1.2.1.1");
    assertMatches("1.2.1-SNAPSHOT");
    assertMatches("1.3.4");
    assertMatches("1.3");

    // bounds are included
    assertMatches("1.2");
    assertMatches("1.5.6");
  }

  @Test
  void version_with_wildcard_for_lower_bound() {
    matcher = new RangedVersionMatcher("*", "1.5.6");
    assertNotMatch("1.5.7");
    assertNotMatch("1.6");
    assertNotMatch("2");

    assertMatches("1.5");
    assertMatches("1.5.5");
    assertMatches("1");
    assertMatches("0.1-SNAPSHOT");
  }

  @Test
  void version_with_wildcard_for_upper_bound() {
    matcher = new RangedVersionMatcher("1.5.6", "*");
    assertNotMatch("1.5");
    assertNotMatch("1.5.5");
    assertNotMatch("1");
    assertNotMatch("0.1-SNAPSHOT");

    assertMatches("1.5.7-SNAPSHOT");
    assertMatches("1.5.7");
    assertMatches("1.6");
    assertMatches("2");
  }

  private void assertMatches(@Nullable String version) {
    assertWithMatcher(version).isTrue();
  }

  private void assertNotMatch(@Nullable String version) {
    assertWithMatcher(version).isFalse();
  }

  private AbstractBooleanAssert<?> assertWithMatcher(@Nullable String version) {
    if (version != null) {
      return assertThat(matcher.test(version));
    }
    return assertThat(matcher.test(null));
  }
}
