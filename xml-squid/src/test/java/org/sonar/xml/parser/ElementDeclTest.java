/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

public class ElementDeclTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.ELEMENT_DECL))
        .matches("<!ELEMENT br EMPTY>")
        .matches("<!ELEMENT p (#PCDATA|emph)* >")
        .matches("<!ELEMENT container ANY>")
        .matches("<!ELEMENT spec (front, body, back?)>")
        .matches("<!ELEMENT div1 (head, (p | list | note)*, div2*)>")
        .matches("<!ELEMENT p (#PCDATA|a|ul|b|i|em)*>")
        .matches("<!ELEMENT b (#PCDATA)>");
  }

  // TEST - FIXME: "Name does not match %, looks like a PEReference, email sent to xml-editors"
  public void w3() {
    assertThat(g.rule(XmlGrammar.ELEMENT_DECL))
        .matches("<!ELEMENT %name.para; %content.para; >")
        .matches("<!ELEMENT dictionary-body (%div.mix; | %dict.mix;)*>")
        .matches("<!ELEMENT p (#PCDATA | %font; | %phrase; | %special; | %form;)* >");
  }

}
