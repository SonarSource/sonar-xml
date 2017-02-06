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

public class EmptyElemTagTest {

  Grammar g = XmlGrammar.createGrammarBuilder().build();

  @Test
  public void ok() {
    assertThat(g.rule(XmlGrammar.EMPTY_ELEM_TAG))
        .matches("<foo />")
        .matches("<foo bar=\"baz\" />")
        .matches("<termdef id=\"dt-dog\" term=\"dog\" />")
        .matches("<br/>")
        .matches("<IMG align=\"left\"\nsrc=\"http://www.w3.org/Icons/WWW/w3c_home\" />");
  }

}
