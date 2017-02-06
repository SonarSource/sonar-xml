/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.xml;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.fest.assertions.Assertions.assertThat;

public class XmlParserConfigurationTest {

  @Test
  public void charsetTest() {
    XmlParserConfiguration.Builder builder = getUTF8Builder();

    builder.setCharset(Charset.forName("UTF-8"));
    assertThat(builder.getCharset().name()).isEqualTo("UTF-8");
    assertThat(builder.build().getCharset().name()).isEqualTo("UTF-8");

    builder.setCharset(Charset.forName("ISO-8859-15"));
    assertThat(builder.getCharset().name()).isEqualTo("ISO-8859-15");
    assertThat(builder.build().getCharset().name()).isEqualTo("ISO-8859-15");
  }

  private XmlParserConfiguration.Builder getUTF8Builder() {
    return XmlParserConfiguration.builder().setCharset(Charset.forName("UTF-8"));
  }

}
