/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xml.XmlPlugin;

/**
 * This class defines the XML language.
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
public class Xml extends AbstractLanguage {

  /** All the valid web files suffixes. */
  private static final String[] DEFAULT_SUFFIXES = { "xml", "xhtml" };

  /** A web instance. */
  public static final Xml INSTANCE = new Xml();

  /** The web language key. */
  public static final String KEY = "xml";

  /** The web language name */
  private static final String XML_LANGUAGE_NAME = "Xml";

  private String[] fileSuffixes;

  /**
   * Default constructor.
   */
  public Xml() {
    super(KEY, XML_LANGUAGE_NAME);

    fileSuffixes = DEFAULT_SUFFIXES;
  }

  public Xml(Project project) {
    this();

    List<?> extensions = project.getConfiguration().getList(XmlPlugin.FILE_EXTENSIONS);

    if (extensions != null && !extensions.isEmpty() && !StringUtils.isEmpty((String) extensions.get(0))) {
      fileSuffixes = new String[extensions.size()];
      for (int i = 0; i < extensions.size(); i++) {
        fileSuffixes[i] = extensions.get(i).toString().trim();
      }
    } else {
      fileSuffixes = DEFAULT_SUFFIXES;
    }
  }

  /**
   * Gets the file suffixes.
   *
   * @return the file suffixes
   * @see org.sonar.api.resources.Language#getFileSuffixes()
   */
  public String[] getFileSuffixes() {
    return fileSuffixes;
  }
}
