/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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

import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;

public abstract class AbstractWebXmlCheck extends SimpleXPathBasedCheck {

  public static final String WEB_XML_ROOT = "web-app";
  private static final String WEB_XML = "web.xml";

  @Override
  public final void scanFile(XmlFile file) {
    if (isWebXmlFile(file)) {
      scanWebXml(file);
    }
  }

  abstract void scanWebXml(XmlFile file);

  private static boolean isWebXmlFile(XmlFile file) {
    return WEB_XML.equalsIgnoreCase(file.getInputFile().filename());
  }
}
