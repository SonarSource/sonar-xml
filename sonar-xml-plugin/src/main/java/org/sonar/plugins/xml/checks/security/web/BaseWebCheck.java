/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks.security.web;

import org.sonar.plugins.xml.Xml;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;

/**
 * Base class for checks targeting Java's web.xml and .NET web.config files.
 */
public class BaseWebCheck extends SimpleXPathBasedCheck {
  protected static final String WEB_XML_ROOT = "web-app";

  @Override
  public final void scanFile(XmlFile file) {
    if (isWebXmlFile(file)) {
      scanWebXml(file);
    } else if (Xml.isDotNetApplicationConfig(file.getInputFile())) {
      scanWebConfig(file);
    }
  }

  /** Scan Java's web.xml. */
  protected void scanWebXml(XmlFile file) {
    // Ignored by default.
  }

  /** Scan .NET's web.config. */
  protected void scanWebConfig(XmlFile file) {
    // Ignored by default.
  }

  private static boolean isWebXmlFile(XmlFile file) {
    return "web.xml".equalsIgnoreCase(file.getInputFile().filename());
  }
}
