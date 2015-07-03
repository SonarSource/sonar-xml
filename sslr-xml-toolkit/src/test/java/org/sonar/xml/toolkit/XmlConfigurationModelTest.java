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
package org.sonar.xml.toolkit;

import com.google.common.base.Charsets;
import org.junit.Test;
import org.sonar.colorizer.Tokenizer;
import org.sonar.sslr.parser.ParserAdapter;
import org.sonar.xml.CDataDocTokenizer;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class XmlConfigurationModelTest {

  @Test
  public void getConfiguration_charset() {
    XmlConfigurationModel model = new XmlConfigurationModel();
    model.charsetProperty.setValue("UTF-8");
    assertThat(model.getConfiguration().getCharset()).isEqualTo(Charsets.UTF_8);
    model.charsetProperty.setValue("ISO-8859-1");
    assertThat(model.getConfiguration().getCharset()).isEqualTo(Charsets.ISO_8859_1);
  }

  @Test
  public void getPropertyOrDefaultValue_with_property_set() {
    String oldValue = System.getProperty("foo");

    try {
      System.setProperty("foo", "bar");
      assertThat(XmlConfigurationModel.getPropertyOrDefaultValue("foo", "baz")).isEqualTo("bar");
    } finally {
      if (oldValue == null) {
        System.clearProperty("foo");
      } else {
        System.setProperty("foo", oldValue);
      }
    }
  }

  @Test
  public void getPropertyOrDefaultValue_with_property_not_set() {
    String oldValue = System.getProperty("foo");

    try {
      System.clearProperty("foo");
      assertThat(XmlConfigurationModel.getPropertyOrDefaultValue("foo", "baz")).isEqualTo("baz");
    } finally {
      if (oldValue == null) {
        System.clearProperty("foo");
      } else {
        System.setProperty("foo", oldValue);
      }
    }
  }

  @Test
  public void getProperties() {
    XmlConfigurationModel model = new XmlConfigurationModel();
    assertThat(model.getProperties()).containsOnly(model.charsetProperty);
  }

  @Test
  public void doGetParser() {
    assertThat(new XmlConfigurationModel().doGetParser()).isInstanceOf(ParserAdapter.class);
  }

  @Test
  public void doGetTokenizers() {
    List<Tokenizer> tokenizers = new XmlConfigurationModel().doGetTokenizers();

    assertThat(tokenizers.size()).isGreaterThan(0);
    assertThat(containsAnyCDataTokenizer(tokenizers)).isEqualTo(true);
  }

  private boolean containsAnyCDataTokenizer(List<Tokenizer> tokenizers) {
    for (Tokenizer tokenizer : tokenizers) {
      if (tokenizer instanceof CDataDocTokenizer) {
        return true;
      }
    }
    return false;
  }

}
