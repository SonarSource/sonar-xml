/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml;

import com.google.common.collect.ImmutableList;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.rules.XmlRulesDefinition;
import org.sonar.plugins.xml.rules.XmlSonarWayProfile;

import java.util.List;

/**
 * XML Plugin publishes extensions to sonar engine.
 *
 * @author Matthijs Galesloot
 */
public final class XmlPlugin extends SonarPlugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.xml.file.suffixes";

  @Override
  public List getExtensions() {
    return ImmutableList.of(

      PropertyDefinition.builder(XmlPlugin.FILE_SUFFIXES_KEY)
        .name("File suffixes")
        .description("Comma-separated list of suffixes for files to analyze.")
        .defaultValue(".xml")
        .category("XML")
        .onQualifiers(Qualifiers.PROJECT)
        .build(),

      Xml.class,

      XmlRulesDefinition.class,
      XmlSonarWayProfile.class,

      // Sensors
      XmlSensor.class);
  }
}
