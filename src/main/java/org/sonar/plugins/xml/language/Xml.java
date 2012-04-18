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

import org.sonar.api.resources.AbstractLanguage;

/**
 * This class defines the XML language.
 * 
 * @author Matthijs Galesloot
 * @since 1.0
 */
public class Xml extends AbstractLanguage {

  /** All the valid xml files suffixes. */
  private static final String[] DEFAULT_SUFFIXES = { "xml", "xhtml" };

  /** A xml instance. */
  public static final Xml INSTANCE = new Xml();

  /** The xml language key. */
  public static final String KEY = "xml";

  /** The xml language name */
  private static final String XML_LANGUAGE_NAME = "Xml";

  /**
   * Default constructor.
   */
  public Xml() {
    super(KEY, XML_LANGUAGE_NAME);
  }

  /**
   * Gets the file suffixes.
   * 
   * @return the file suffixes
   * @see org.sonar.api.resources.Language#getFileSuffixes()
   */
  public String[] getFileSuffixes() {
    return DEFAULT_SUFFIXES; 
  }
}
