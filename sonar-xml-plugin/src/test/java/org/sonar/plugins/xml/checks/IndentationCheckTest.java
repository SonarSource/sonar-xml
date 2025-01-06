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
package org.sonar.plugins.xml.checks;

import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheckVerifier;

class IndentationCheckTest {

  @Test
  void test() {
    IndentationCheck check = new IndentationCheck();
    SonarXmlCheckVerifier.verifyIssues("IndentationCheck.xml", check);
  }

  @Test
  void test_with_parameters() {
    IndentationCheck check = new IndentationCheck();
    check.setIndentSize(4);
    check.setTabSize(4);
    SonarXmlCheckVerifier.verifyIssues("IndentationCheckCustom.xml", check);
  }

}
