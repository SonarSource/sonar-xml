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
package org.sonar.plugins.xml.parsers;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.xml.sax.SAXException;

/**
 * Provides reusable code for Xml parsers.
 *
 * @author Matthijs Galesloot
 */
public abstract class AbstractParser {

  private static final SAXParserFactory SAX_FACTORY_NAMESPACE_AWARE;
  private static final SAXParserFactory SAX_FACTORY_NAMESPACE_UNAWARE;

  /**
   * Build the SAXParserFactory.
   */
  static {

    SAX_FACTORY_NAMESPACE_AWARE = new SAXParserFactoryImpl();
    SAX_FACTORY_NAMESPACE_UNAWARE = new SAXParserFactoryImpl();

    setCommonConf(SAX_FACTORY_NAMESPACE_AWARE);
    SAX_FACTORY_NAMESPACE_AWARE.setNamespaceAware(true);

    setCommonConf(SAX_FACTORY_NAMESPACE_UNAWARE);
    SAX_FACTORY_NAMESPACE_UNAWARE.setNamespaceAware(false);
  }

  protected SAXParser newSaxParser(boolean namespaceAware) {
    try {
      return namespaceAware ? SAX_FACTORY_NAMESPACE_AWARE.newSAXParser() : SAX_FACTORY_NAMESPACE_UNAWARE.newSAXParser();
    } catch (SAXException | ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void setCommonConf(SAXParserFactory factory) {
    try {
      factory.setValidating(false);
      factory.setFeature("http://xml.org/sax/features/validation", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    } catch (SAXException | ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

}
