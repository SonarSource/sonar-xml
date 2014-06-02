/*
 * SonarQube XML Plugin
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
package org.sonar.xml;

import org.sonar.colorizer.MultilinesDocTokenizer;
import org.sonar.colorizer.RegexpTokenizer;
import org.sonar.colorizer.Tokenizer;

import java.util.Arrays;
import java.util.List;

public final class XmlColorizer {

  private static final String END_TAG = "</span>";

  private XmlColorizer() {
  }

  public static List<Tokenizer> createTokenizers() {
    return Arrays.asList(
        new CDataDocTokenizer(span("k"), END_TAG),
        new RegexpTokenizer(span("j"), END_TAG, "<!DOCTYPE.*>"),
        new MultilinesDocTokenizer("<!--", "-->", span("j"), END_TAG),
        new MultilinesDocTokenizer("</", ">", span("k"), END_TAG),
        new XmlStartElementTokenizer(span("k"), END_TAG, span("c"), END_TAG, span("s"), END_TAG));
  }

  private static String span(String clazz) {
   return  "<span class=\"" + clazz + "\">";
  }
}
