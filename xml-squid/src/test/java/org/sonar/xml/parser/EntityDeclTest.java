/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

public class EntityDeclTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.ENTITY_DECL))
        .matches("<!ENTITY foo 'foo'>")
        .matches("<!ENTITY % foo 'foo'>")

        .matches("<!ENTITY d \"&#xD;\">")
        .matches("<!ENTITY a \"&#xA;\">")
        .matches("<!ENTITY da \"&#xD;&#xA;\">")

        .matches("<!ENTITY % draft 'INCLUDE' >")
        .matches("<!ENTITY % final 'IGNORE' >")

        .matches("<!ENTITY % ISOLat2\nSYSTEM \"http://www.xml.com/iso/isolat2-xml.entities\" >")

        .matches("<!ENTITY Pub-Status \"This is a pre-release of the\nspecification.\">")

        .matches("<!ENTITY open-hatch\nSYSTEM \"http://www.textuality.com/boilerplate/OpenHatch.xml\">")
        .matches("<!ENTITY open-hatch\nPUBLIC \"-//Textuality//TEXT Standard open-hatch boilerplate//EN\"\n\"http://www.textuality.com/boilerplate/OpenHatch.xml\">")
        .matches("<!ENTITY hatch-pic\nSYSTEM \"../grafix/OpenHatch.gif\"\nNDATA gif >")

        .matches("<!ENTITY % YN '\"Yes\"' >")
        .matches("<!ENTITY WhatHeSaid \"He said %YN;\" >")

        .matches("<!ENTITY % pub    \"&#xc9;ditions Gallimard\" >")
        .matches("<!ENTITY   rights \"All rights reserved\" >")
        .matches("<!ENTITY   book   \"La Peste: Albert Camus,\n&#xA9; 1947 %pub;. &rights;\" >")

        .matches("<!ENTITY lt     \"&#38;#60;\">")
        .matches("<!ENTITY gt     \"&#62;\">")
        .matches("<!ENTITY amp    \"&#38;#38;\">")
        .matches("<!ENTITY apos   \"&#39;\">")
        .matches("<!ENTITY quot   \"&#34;\">");
  }

}
