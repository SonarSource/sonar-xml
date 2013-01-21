/*
 * Sonar XML Plugin
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

import com.google.common.collect.Lists;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.xml.language.Xml;

import java.io.File;
import java.util.List;

/**
 * Import of XML source files to sonar database.
 *
 * @author Matthijs Galesloot
 */
public final class XmlSourceImporter extends AbstractSourceImporter {

  private final Project project;
  private final Settings settings;

  public XmlSourceImporter(Xml xml, Project project, Settings settings) {
    super(xml);
    this.project = project;
    this.settings = settings;
  }

  @Override
  protected void analyse(ProjectFileSystem fileSystem, SensorContext context) {
    List<File> files = toFiles(XmlPlugin.getFiles(project, settings));
    List<File> dirs = XmlProjectFileSystem.getSourceDirs(project);
    parseDirs(context, files, dirs, false, fileSystem.getSourceCharset());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  /**
   * Conversion from InputFile to File. Allows to provide backward compatibility.
   */
  private static List<File> toFiles(List<InputFile> files) {
    List<File> result = Lists.newArrayList();
    for (InputFile file : files) {
      result.add(file.getFile());
    }
    return result;
  }

}
