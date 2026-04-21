/*
 * SonarQube XML Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.PropertyDefinition.ConfigScope;

public final class XmlPlugin implements Plugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.xml.file.suffixes";

  @Override
  public void define(Context context) {
    context.addExtensions(
      PropertyDefinition.builder(XmlPlugin.FILE_SUFFIXES_KEY)
        .name("File suffixes")
        .description("List of suffixes for XML files to analyze.")
        .defaultValue(".xml,.xsd,.xsl,.config")
        .multiValues(true)
        .category("XML")
        .onConfigScopes(ConfigScope.PROJECT)
        .build(),
      Xml.class,
      XmlRulesDefinition.class,
      XmlSonarWayProfile.class,
      XmlSensor.class);
  }

}
