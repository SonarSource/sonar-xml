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

public class DocumentTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.DOCUMENT))
        .matches("<br />")
        .matches("<p><br /></p>")
        .matches("<?xml version='1.0'?><br />")

        .matches("<?xml version=\"1.1\"?>\n<!DOCTYPE greeting SYSTEM \"hello.dtd\">\n<greeting>Hello, world!</greeting>")
        .matches("<?xml version=\"1.1\" encoding=\"UTF-8\" ?><!DOCTYPE greeting [\n<!ELEMENT greeting (#PCDATA)>\n]>\n<greeting>Hello, world!</greeting>");
  }

}
