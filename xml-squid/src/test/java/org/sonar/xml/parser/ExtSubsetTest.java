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

public class ExtSubsetTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.EXT_SUBSET))
        .matches("")
        .matches("<!ELEMENT foo EMPTY>")
        .matches("<!ELEMENT foo EMPTY>  <![IGNORE[]]>")
        .matches("<?xml encoding='foo'?>")
        .matches("<?xml encoding='foo'?><!ELEMENT foo EMPTY>");
  }

}
