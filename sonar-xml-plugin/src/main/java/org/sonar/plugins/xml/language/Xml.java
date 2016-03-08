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
