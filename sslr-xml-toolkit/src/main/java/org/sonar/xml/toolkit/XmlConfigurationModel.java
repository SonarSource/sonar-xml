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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.sonar.sslr.api.Grammar;
import com.sonar.sslr.impl.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.colorizer.MultilinesDocTokenizer;
import org.sonar.colorizer.RegexpTokenizer;
import org.sonar.colorizer.Tokenizer;
import org.sonar.sslr.parser.ParserAdapter;
import org.sonar.sslr.toolkit.AbstractConfigurationModel;
import org.sonar.sslr.toolkit.ConfigurationProperty;
import org.sonar.sslr.toolkit.Validators;
import org.sonar.xml.XmlParserConfiguration;
import org.sonar.xml.api.XmlGrammar;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class XmlConfigurationModel extends AbstractConfigurationModel {

  private static final Logger LOG = LoggerFactory.getLogger(XmlConfigurationModel.class);

  private static final String CHARSET_PROPERTY_KEY = "sonar.sourceEncoding";

  @VisibleForTesting
  ConfigurationProperty charsetProperty = new ConfigurationProperty("Charset", CHARSET_PROPERTY_KEY,
      getPropertyOrDefaultValue(CHARSET_PROPERTY_KEY, "UTF-8"),
      Validators.charsetValidator());

  public List<ConfigurationProperty> getProperties() {
    return ImmutableList.of(charsetProperty);
  }

  @Override
  public Parser<? extends Grammar> doGetParser() {
    return new ParserAdapter(getCharset(), XmlGrammar.createGrammarBuilder().build());
  }

  @Override
  public List<Tokenizer> doGetTokenizers() {
    return Arrays.asList(
        new CDataDocTokenizer("<span class=\"k\">", "</span>"),
        new RegexpTokenizer("<span class=\"j\">", "</span>", "<!DOCTYPE.*>"),
        new MultilinesDocTokenizer("<!--", "-->", "<span class=\"j\">", "</span>"),
        new MultilinesDocTokenizer("</", ">", "<span class=\"k\">", "</span>"),
        new XmlStartElementTokenizer("<span class=\"k\">", "</span>", "<span class=\"c\">", "</span>", "<span class=\"s\">", "</span>"));
  }

  @VisibleForTesting
  XmlParserConfiguration getConfiguration() {
    return XmlParserConfiguration.builder()
        .setCharset(Charset.forName(charsetProperty.getValue()))
        .build();
  }

  @VisibleForTesting
  static String getPropertyOrDefaultValue(String propertyKey, String defaultValue) {
    String propertyValue = System.getProperty(propertyKey);

    if (propertyValue == null) {
      LOG.info("The property \"" + propertyKey + "\" is not set, using the default value \"" + defaultValue + "\".");
      return defaultValue;
    } else {
      LOG.info("The property \"" + propertyKey + "\" is set, using its value \"" + propertyValue + "\".");
      return propertyValue;
    }
  }

}
