/*
 * Sonar XML Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
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

import org.junit.Test;
import org.sonar.colorizer.Tokenizer;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class XmlColorizerTest {

  @Test
  public void getTokenizers() {
    List<Tokenizer> tokenizers = XmlColorizer.getTokenizers();

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
