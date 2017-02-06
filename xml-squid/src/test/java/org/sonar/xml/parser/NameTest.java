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

public class NameTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.NAME))
        .matches("foo")
        .matches("foo42")

        .matches(":")
        .matches("F")
        .matches("_")
        .matches("h")
        .matches("\u00D0")
        .matches("\u00D9")

        .notMatches("\u00D7")

        .notMatches("-")
        .notMatches("\u200E")

        .notMatches("foo?")
        .notMatches("foo ");
  }

}
