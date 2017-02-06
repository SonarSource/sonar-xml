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
