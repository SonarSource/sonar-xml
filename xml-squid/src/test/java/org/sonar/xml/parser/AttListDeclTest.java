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

public class AttListDeclTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.ATT_LIST_DECL))
        .matches("<!ATTLIST foo>")
        .matches("<!ATTLIST foo >")
        .matches("<!ATTLIST foo bar ID #REQUIRED>")
        .matches("<!ATTLIST foo bar ID #REQUIRED baz CDATA #IMPLIED >")

        .matches("<!ATTLIST poem  xml:space (default|preserve) 'preserve'>")
        .matches("<!ATTLIST pre xml:space (preserve) #FIXED 'preserve'>")

        .matches("<!ATTLIST poem   xml:lang CDATA 'fr'>")
        .matches("<!ATTLIST gloss  xml:lang CDATA 'en'>")
        .matches("<!ATTLIST note   xml:lang CDATA 'en'>")

        .matches("<!ATTLIST termdef\nid      ID      #REQUIRED\nname    CDATA   #IMPLIED>")
        .matches("<!ATTLIST list\ntype    (bullets|ordered|glossary)  \"ordered\">")
        .matches("<!ATTLIST form\nmethod  CDATA   #FIXED \"POST\">");
  }

}
