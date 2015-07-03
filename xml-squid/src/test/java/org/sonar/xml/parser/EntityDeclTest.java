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
