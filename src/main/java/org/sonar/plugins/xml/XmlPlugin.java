/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
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

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.language.XmlCodeColorizerFormat;
import org.sonar.plugins.xml.rules.DefaultXmlProfile;
import org.sonar.plugins.xml.rules.XhtmlTransitionalProfile;
import org.sonar.plugins.xml.rules.XmlMessagesRepository;
import org.sonar.plugins.xml.rules.XmlRulesRepository;
import org.sonar.plugins.xml.rules.XmlSchemaMessagesRepository;

/**
 * Web Plugin publishes extensions to sonar engine.
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
@Properties({
    @Property(key = XmlPlugin.FILE_EXTENSIONS, name = "File extensions", description = "List of file extensions that will be scanned.",
        defaultValue = "xml,xhtml", global = true, project = true),
    @Property(key = XmlPlugin.SOURCE_DIRECTORY, name = "Source directory", description = "Source directory that will be scanned.",
        defaultValue = "src/main/resources", global = false, project = true) })
public final class XmlPlugin implements Plugin {

  public static final String FILE_EXTENSIONS = "sonar.web.fileExtensions";

  private static final String KEY = "sonar-xml-plugin";
  public static final String SOURCE_DIRECTORY = "sonar.web.sourceDirectory";

  public static void configureSourceDir(Project project) {
    String sourceDir = (String) project.getProperty(SOURCE_DIRECTORY);
    if (sourceDir != null) {
      project.getPom().getCompileSourceRoots().clear();
      project.getPom().addCompileSourceRoot(sourceDir);
    }
  }

  public String getDescription() {
    return getName() + " collects metrics on XML documents, such as lines of code, schema validation, violations ...";
  }

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();

    // xml language
    list.add(Xml.class);

    // xml files importer
    list.add(XmlSourceImporter.class);

    // XML rules and messages
    list.add(XmlRulesRepository.class);
    list.add(XmlMessagesRepository.class);
    list.add(XmlSchemaMessagesRepository.class);

    // Profiles
    list.add(DefaultXmlProfile.class);
    list.add(XhtmlTransitionalProfile.class);

    // sensors
    list.add(XmlSensor.class);
    list.add(LineCountSensor.class);

    // Code Colorizer
    list.add(XmlCodeColorizerFormat.class);

    return list;
  }

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return "Xml plugin";
  }

  @Override
  public String toString() {
    return getKey();
  }
}
