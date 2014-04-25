/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.xml;

import com.google.common.collect.ImmutableList;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.language.XmlCodeColorizerFormat;
import org.sonar.plugins.xml.rules.XmlRulesRepository;
import org.sonar.plugins.xml.rules.XmlSonarWayProfile;

import java.util.List;

/**
 * XML Plugin publishes extensions to sonar engine.
 *
 * @author Matthijs Galesloot
 */
public final class XmlPlugin extends SonarPlugin {

  public static final String FILE_EXTENSIONS = "sonar.xml.file.suffixes";

  public List getExtensions() {
    return ImmutableList.of(

      PropertyDefinition.builder(XmlPlugin.FILE_EXTENSIONS)
        .name("File extensions")
        .description("List of file extensions that will be scanned.")
        .defaultValue("xml,xhtml")
        .category("XML")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT),

      Xml.class,

      XmlSourceImporter.class,

      XmlRulesRepository.class,
      XmlSonarWayProfile.class,

      // Sensors
      XmlSensor.class,
      LineCountSensor.class,

      XmlCodeColorizerFormat.class);
  }
}
