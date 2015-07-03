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
