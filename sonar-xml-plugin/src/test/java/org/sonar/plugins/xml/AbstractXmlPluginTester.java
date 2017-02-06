/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml;

import javax.xml.XMLConstants;

/**
 * @author Matthijs Galesloot
 */
public class AbstractXmlPluginTester {

  static {
    System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI,
      "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
  }
}
