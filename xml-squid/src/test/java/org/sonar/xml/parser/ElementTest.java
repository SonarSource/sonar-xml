/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
