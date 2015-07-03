/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
