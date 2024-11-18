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

import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MavenDependencyMatcherTest {

  private MavenDependencyMatcher matcher;

  private static final String DEPENDENCY_NAME_HELP = " Should match '[groupId]:[artifactId]', you can use '*' as wildcard or a regular expression.";
  private static final String DEPENDENCY_VERSION_HELP = " Leave blank for all versions. You can use '*' as wildcard and '-' as range like '1.0-3.1' or '*-3.1'.";

  @ParameterizedTest
  @CsvSource(delimiter = '|', value = {
    "junit:junit:junit||" +
      "Invalid DependencyName pattern 'junit:junit:junit'." + DEPENDENCY_NAME_HELP + " Error: Only one ':' separator expected.",
    "||" +
      "Invalid DependencyName pattern ''." + DEPENDENCY_NAME_HELP + " Error: Missing ':' separator.",
    "org(:||" +
      "Invalid DependencyName pattern 'org(:'." + DEPENDENCY_NAME_HELP + " Error: Unable to compile the regular expression 'org(', "/* ... */,
    "org.apache:log4j|1\\.2(|" +
      "Invalid Version pattern '1\\.2('." + DEPENDENCY_VERSION_HELP + " Error: Unable to compile the regular expression '1\\.2(', "/* ... */
  })
  void invalid_arguments_format_should_fail(@Nullable String dependencyName, @Nullable String dependencyVersion, String expected) {
    assertThatThrownBy(() -> new MavenDependencyMatcher(
      dependencyName != null ? dependencyName : "",
      dependencyVersion != null ? dependencyVersion : ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith(expected);
  }

  @Test
  void empty_dependencies_never_match() {
    matcher = new MavenDependencyMatcher("*:log", "");
    assertNotMatch("", "", "");
  }

  @Test
  void should_handle_wildcards_for_groupId() {
    matcher = new MavenDependencyMatcher("*:log", "");
    assertMatches("a.b.c", "log");
    assertNotMatch("log", "d");
  }

  @Test
  void should_handle_wildcards_for_artifactId() {
    matcher = new MavenDependencyMatcher("log:*", "");
    assertNotMatch("a.b.c", "log");
    assertMatches("log", "d");
  }

  @Test
  void should_handle_exact_values() {
    matcher = new MavenDependencyMatcher("log:log", "");
    assertNotMatch("log", "a");
    assertNotMatch("a.b.c", "log");
    assertMatches("log", "log");
  }

  @Test
  void should_handle_fixed_versions() {
    matcher = new MavenDependencyMatcher("log:log", "1.3");
    assertNotMatch("log", "a");
    assertNotMatch("a.b.c", "log");
    assertNotMatch("log", "log");
    assertNotMatch("log", "log", "1.2");
    assertMatches("log", "log", "1.3");
  }

  @Test
  void should_handle_pattern_version() {
    matcher = new MavenDependencyMatcher("log:log", "1.3.*");
    assertNotMatch("log", "a");
    assertNotMatch("a.b.c", "log");
    assertNotMatch("log", "log");
    assertNotMatch("log", "log", "1.2");
    assertMatches("log", "log", "1.3");
    assertMatches("log", "log", "1.3-SNAPSHOT");
  }

  @Test
  void should_handle_ranged_versions() {
    matcher = new MavenDependencyMatcher("log:log", "1.2.5-2");
    assertNotMatch("log", "a");
    assertNotMatch("a.b.c", "log");
    assertNotMatch("log", "log");
    assertNotMatch("log", "log", "1.2");
    assertMatches("log", "log", "1.3");
  }

  private void assertNotMatch(String groupId, String artifactId) {
    assertNotMatch(groupId, artifactId, "");
  }

  private void assertNotMatch(String groupId, String artifactId, @Nullable String version) {
    assertThat(matcher.matches(groupId, artifactId, version)).isFalse();
  }

  private void assertMatches(String groupId, String artifactId) {
    assertMatches(groupId, artifactId, "");
  }

  private void assertMatches(String groupId, String artifactId, String version) {
    assertThat(matcher.matches(groupId, artifactId, version)).isTrue();
  }
}
