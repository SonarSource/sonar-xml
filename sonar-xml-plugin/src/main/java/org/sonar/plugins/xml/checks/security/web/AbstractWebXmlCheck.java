/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
