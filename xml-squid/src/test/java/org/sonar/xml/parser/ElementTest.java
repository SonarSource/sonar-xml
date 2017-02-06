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

public class ElementTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.ELEMENT))
        .matches("<foo />")
        .matches("<foo></foo>")
        .matches("<foo>bar</foo>")
        .matches("<foo><bar /></foo>")
        .matches("<foo><bar depth=\"2\">baz</bar></foo>")

        .matches("<p xml:lang=\"en\">The quick brown fox jumps over the lazy dog.</p>")
        .matches("<p xml:lang=\"en-GB\">What colour is it?</p>")
        .matches("<p xml:lang=\"en-US\">What color is it?</p>")
        .matches("<sp who=\"Faust\" desc='leise' xml:lang=\"de\">\n" +
          "<l>Habe nun, ach! Philosophie,</l>\n" +
          "<l>Juristerei, und Medizin</l>\n" +
          "<l>und leider auch Theologie</l>\n" +
          "<l>durchaus studiert mit hei&#xDF;em Bem&#xFC;h'n.</l>\n" +
          "</sp>");
  }

}
