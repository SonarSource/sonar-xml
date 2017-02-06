/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
