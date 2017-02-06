/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.xml.parser;

import com.sonar.sslr.api.Grammar;
import org.junit.Test;
import org.sonar.xml.api.XmlGrammar;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class AttValueTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.ATT_VALUE))
        .matches("\"\"")
        .matches("\"foo\"")
        .matches("\"foo&bar;baz\"")

        .matches("''")
        .matches("'foo'")
        .matches("'foo&bar;baz'")

        // Ordinary characters, not a PE_REFERENCE
        .matches("'%foo'")

        .notMatches("\"<\"")
        .notMatches("\"foo<bar\"")
        .notMatches("'foo<bar'")

        .notMatches("\"'")
        .notMatches("'\"")
        .notMatches("'&foo'");
  }

}
