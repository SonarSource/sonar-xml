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

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

public class RangedVersionMatcher implements StringMatcher {
  @Nullable
  private final ArtifactVersion lowerBound;
  @Nullable
  private final ArtifactVersion upperBound;

  public RangedVersionMatcher(String lowerBound, String upperBound) {
    try {
      this.lowerBound = isWildCard(lowerBound) ? null : getVersion(lowerBound);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Invalid version range lower bound  '" + lowerBound + "'. " + e.getMessage());
    }
    try {
      this.upperBound = isWildCard(upperBound) ? null : getVersion(upperBound);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Invalid version range upper bound  '" + upperBound + "'. " + e.getMessage());
    }
    // check that we are not bypassing both bounds
    if ((this.lowerBound == null && this.upperBound == null)) {
      throw new IllegalArgumentException("Version range '*-*' is not valid. You should specify at least the lower or the upper bound.");
    }
  }

  private static boolean isWildCard(String pattern) {
    return "*".equals(pattern);
  }

  /**
   * Build a {@link ArtifactVersion} from a String, throwing an {@link IllegalArgumentException} if failed to parse value.
   * @param version the raw version as string
   * @return the {@link ArtifactVersion} corresponding to the provided version as string
   */
  private static ArtifactVersion getVersion(String version) {
    try {
      return ArtifactVersion.parseString(version);
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Unsupported version format '" + version + "'. " +
        "The version does not match expected pattern: '<major version>.<minor version>.<incremental version>'");
    }
  }

  @Override
  public boolean test(@Nullable String value) {
    if (value == null) {
      return false;
    }
    ArtifactVersion dependencyVersion = getVersionSilently(value);
    if (dependencyVersion == null) {
      // unable to parse version, ignore this dependency
      return false;
    }
    boolean matchesLowerBound = lowerBound == null || dependencyVersion.isAfterOrEqual(lowerBound);
    boolean matchesUpperBound = upperBound == null || upperBound.isAfterOrEqual(dependencyVersion);
    return matchesLowerBound && matchesUpperBound;
  }

  /**
   * Build a {@link ArtifactVersion} from a String, without failing in case of exception while parsing.
   * @param version the raw version as string
   * @return the {@link ArtifactVersion} corresponding to the provided version as string
   */
  @CheckForNull
  private static ArtifactVersion getVersionSilently(String version) {
    try {
      return ArtifactVersion.parseString(version);
    } catch (NumberFormatException e) {
      // do nothing, version will be ignored
    }
    return null;
  }

  /**
   * Expected format: <code>[major version].[minor version].[incremental version]-[qualifier]</code>
   *
   * <br />
   *
   * Handled formats such as:
   * <ul>
   *   <li><code>1</code></li>
   *   <li><code>2.4</code></li>
   *   <li><code>0.5-alpha</code></li>
   *   <li><code>1.0.1-SNAPSHOT</code></li>
   * </ul>
   */
  private static class ArtifactVersion {
    private static final int HANDLED_VERSIONS = 3;
    private final Integer[] versions = new Integer[HANDLED_VERSIONS];

    private static ArtifactVersion parseString(String version) {
      String[] split = version.split("\\.");
      if (version.contains("-")) {
        // ignore qualifier
        split = version.split("-")[0].split("\\.");
      }
      ArtifactVersion result = new ArtifactVersion();
      for (int i = 0; i < Math.min(HANDLED_VERSIONS, split.length); i++) {
        result.versions[i] = Integer.parseInt(split[i]);
      }
      return result;
    }

    public boolean isAfterOrEqual(ArtifactVersion v) {
      return isAfterOrEqual(v, 0);
    }

    public boolean isAfterOrEqual(ArtifactVersion v, int i) {
      if (i >= HANDLED_VERSIONS) {
        return true;
      }
      Integer local = versions[i];
      Integer target = v.versions[i];

      if (target == null) {
        return true;
      } else if (local == null) {
        return false;
      }

      int compare = local.compareTo(target);
      return (compare > 0) || (compare == 0 && isAfterOrEqual(v, i + 1));
    }
  }
}
