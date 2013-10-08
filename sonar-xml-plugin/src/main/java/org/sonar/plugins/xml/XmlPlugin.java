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

import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.language.XmlCodeColorizerFormat;
import org.sonar.plugins.xml.rules.XmlRulesRepository;
import org.sonar.plugins.xml.rules.XmlSonarWayProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * XML Plugin publishes extensions to sonar engine.
 *
 * @author Matthijs Galesloot
 */
@Properties({
  @Property(key = XmlPlugin.FILE_EXTENSIONS, name = "File extensions", description = "List of file extensions that will be scanned.",
    defaultValue = "xml,xhtml", global = true, project = true),
  @Property(key = XmlPlugin.INCLUDE_FILE_FILTER, name = "Files to include",
    description = "List of file inclusion filters, separated by comma.", defaultValue = "", global = false, project = false),
  @Property(key = XmlPlugin.SOURCE_DIRECTORY, name = "Source directory", description = "Source directory that will be scanned.",
    defaultValue = "src/main/resources", global = false, project = false)})
public final class XmlPlugin extends SonarPlugin {

  public static final String FILE_EXTENSIONS = "sonar.xml.fileExtensions";
  public static final String SOURCE_DIRECTORY = "sonar.xml.sourceDirectory";
  public static final String INCLUDE_FILE_FILTER = "sonar.xml.includeFileFilter";

  public static List<InputFile> getFiles(Project project, Settings settings) {
    XmlProjectFileSystem fileSystem = new XmlProjectFileSystem(project);
    return fileSystem.getFiles(settings);
  }

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();

    // xml language
    list.add(Xml.class);

    // xml files importer
    list.add(XmlSourceImporter.class);

    // XML rules
    list.add(XmlRulesRepository.class);

    // Profiles
    list.add(XmlSonarWayProfile.class);

    // sensors
    list.add(XmlSensor.class);
    list.add(LineCountSensor.class);

    // Code Colorizer
    list.add(XmlCodeColorizerFormat.class);

    return list;
  }
}
