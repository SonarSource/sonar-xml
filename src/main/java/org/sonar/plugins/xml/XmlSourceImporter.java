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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.File;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.xml.language.Xml;

import com.google.common.collect.Lists;

/**
 * Import of XML source files to sonar database.
 * 
 * @author Matthijs Galesloot
 * @since 1.0
 */
public final class XmlSourceImporter extends AbstractSourceImporter {

  private static final Logger LOG = LoggerFactory.getLogger(XmlSourceImporter.class);

  public XmlSourceImporter(Xml xml) {
    super(xml);
  }

  /**
   * Conversion from InputFile to File. Allows to provide backward compatibility.
   */
  private static List<java.io.File> toFiles(List<InputFile> files) {
    List<java.io.File> result = Lists.newArrayList();
    for (InputFile file : files) {
      result.add(file.getFile());
    }
    return result;
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    parseDirs(context, toFiles(XmlPlugin.getFiles(project)), XmlProjectFileSystem.getSourceDirs(project), false, project.getFileSystem()
        .getSourceCharset());
  }

  @Override
  protected Resource<?> createResource(java.io.File file, List<java.io.File> sourceDirs, boolean unitTest) {
    LOG.debug("XmlSourceImporter:" + file.getPath());
    return File.fromIOFile(file, sourceDirs);
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return isEnabled(project) && StringUtils.equals(Xml.KEY, project.getLanguageKey());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}