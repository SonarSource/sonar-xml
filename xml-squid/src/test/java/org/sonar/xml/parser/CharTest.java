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

public class CharTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.CHAR))
        .matches("\u0001")
        .matches("\u1234")
        .matches("\uD7FF")
        .matches("\uE000")
        .matches("\uFFFD")

        .notMatches("\uD800")
        .notMatches("\uFFFF");
  }

}
