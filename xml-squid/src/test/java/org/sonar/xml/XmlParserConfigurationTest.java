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
