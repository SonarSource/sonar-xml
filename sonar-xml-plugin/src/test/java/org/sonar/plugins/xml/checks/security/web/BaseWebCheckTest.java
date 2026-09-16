/*
 * SonarQube XML Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks.security.web;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BaseWebCheckTest {
  @Test
  void testGetDeepestExistingNode() {
    BaseWebCheck baseWebCheck = new BaseWebCheck();

    assertThat(baseWebCheck.getDeepestExistingNode("a"))
      .isEqualTo("/a");

    assertThat(baseWebCheck.getDeepestExistingNode("a", "b"))
      .isEqualTo("/a/b|/a[not(b)]");

    assertThat(baseWebCheck.getDeepestExistingNode("a", "b", "c"))
      .isEqualTo("/a/b/c|/a/b[not(c)]|/a[not(b)]");
  }
}
