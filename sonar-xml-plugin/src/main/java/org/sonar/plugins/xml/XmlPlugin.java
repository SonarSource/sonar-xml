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
package org.sonar.plugins.xml;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.rules.XmlRulesDefinition;
import org.sonar.plugins.xml.rules.XmlSonarWayProfile;

/**
 * XML Plugin publishes extensions to sonar engine.
 *
 * @author Matthijs Galesloot
 */
public final class XmlPlugin implements Plugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.xml.file.suffixes";

  @Override
  public void define(Context context) {
    context.addExtensions(
      PropertyDefinition.builder(XmlPlugin.FILE_SUFFIXES_KEY)
        .name("File suffixes")
        .description("Comma-separated list of suffixes for files to analyze.")
        .defaultValue(".xml,.xsd,.xsl")
        .category("XML")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),
      PropertyDefinition.builder(XmlSensor.FILENAME_LIST_TO_EXCLUDE_FROM_LOC_METRIC_KEY)
        .name("Filenames to ignore when counting lines of code")
        .description("Comma-separated list of file name patterns to skip when counting lines of code. You may use regular expressions as long as they don't contain commas. Usage example: You generate an effective-pom.xml file during your scan and want your Sonarqube rules to run against it BUT don't want the line count for that file captured.")
        .defaultValue("")
        .category("XML")
        .build(),
      Xml.class,
      XmlRulesDefinition.class,
      XmlSonarWayProfile.class,
      XmlSensor.class);
  }

}
