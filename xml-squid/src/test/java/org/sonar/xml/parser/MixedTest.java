/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.xml.parser;

import com.sonar.sslr.api.Grammar;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.xml.api.XmlGrammar;

import static org.sonar.sslr.tests.Assertions.assertThat;

public class MixedTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.MIXED))
        .matches("(#PCDATA)")
        .matches("( #PCDATA )")
        .matches("(#PCDATA)*")
        .matches("( #PCDATA )*")
        .matches("(#PCDATA|foo)*")
        .matches("(#PCDATA|foo|bar)*")
        .matches("(#PCDATA | foo | bar)*")

        .matches("(#PCDATA|a|ul|b|i|em)*")
        .matches("(#PCDATA)");
  }

  // TEST - FIXME: Name does not match %, looks like a PEReference, email sent to xml-editors
  public void w3() {
    assertThat(g.rule(XmlGrammar.MIXED))
        .matches("(#PCDATA | %font; | %phrase; | %special; | %form;)*");
  }

}
