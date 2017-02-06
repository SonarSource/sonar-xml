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
