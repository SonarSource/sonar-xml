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
package org.sonar.plugins.xml.language;

import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.MultilinesDocTokenizer;
import org.sonar.colorizer.RegexpTokenizer;
import org.sonar.colorizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class XmlCodeColorizerFormat extends CodeColorizerFormat {

  private final List<Tokenizer> tokenizers = new ArrayList<Tokenizer>();

  public XmlCodeColorizerFormat() {
    super(Xml.KEY);
    String tagAfter = "</span>";

    // == CDATA ==
    tokenizers.add(new CDATADocTokenizer("<span class=\"k\">", tagAfter));

    // == doctype ==
    tokenizers.add(new RegexpTokenizer("<span class=\"j\">", tagAfter, "<!DOCTYPE.*>"));

    // == comments ==
    tokenizers.add(new MultilinesDocTokenizer("<!--", "-->", "<span class=\"j\">", tagAfter));

    // == tags ==
    tokenizers.add(new MultilinesDocTokenizer("</", ">", "<span class=\"k\">", tagAfter));
    tokenizers.add(new XmlStartElementTokenizer("<span class=\"k\">", tagAfter, "<span class=\"c\">", tagAfter, "<span class=\"s\">", tagAfter));

  }

  @Override
  public List<Tokenizer> getTokenizers() {
    return tokenizers;
  }

}
