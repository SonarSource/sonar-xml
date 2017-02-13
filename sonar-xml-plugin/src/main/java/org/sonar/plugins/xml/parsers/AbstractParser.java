/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
