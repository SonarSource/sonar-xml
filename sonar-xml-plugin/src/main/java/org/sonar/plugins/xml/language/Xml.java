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
package org.sonar.plugins.xml.language;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.plugins.xml.XmlPlugin;

import java.util.List;

/**
 * This class defines the XML language.
 *
 * @author Matthijs Galesloot
 */
public class Xml extends AbstractLanguage {

  /** All the valid xml files suffixes. */
  private static final String[] DEFAULT_SUFFIXES = {".xml"};

  /** The xml language key. */
  public static final String KEY = "xml";

  /** The xml language name */
  private static final String XML_LANGUAGE_NAME = "XML";

  private Settings settings;

  /**
   * Default constructor.
   */
  public Xml(Settings settings) {
    super(KEY, XML_LANGUAGE_NAME);
    this.settings = settings;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = filterEmptyStrings(settings.getStringArray(XmlPlugin.FILE_SUFFIXES_KEY));
    if (suffixes.length == 0) {
      suffixes = Xml.DEFAULT_SUFFIXES;
    }
    return suffixes;
  }

  private static String[] filterEmptyStrings(String[] stringArray) {
    List<String> nonEmptyStrings = Lists.newArrayList();
    for (String string : stringArray) {
      if (StringUtils.isNotBlank(string.trim())) {
        nonEmptyStrings.add(string.trim());
      }
    }
    return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
  }

}
