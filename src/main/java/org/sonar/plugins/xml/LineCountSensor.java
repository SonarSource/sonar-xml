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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.parsers.LineCountParser;

import java.io.File;
import java.io.IOException;

/**
 * Count lines of code in XML files.
 *
 * @author Matthijs Galesloot
 */
public final class LineCountSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(LineCountSensor.class);
  private Settings settings;

  public LineCountSensor(Settings settings) {
    this.settings = settings;
  }

  private void addMeasures(SensorContext sensorContext, File file, org.sonar.api.resources.File xmlFile) {

    LineIterator iterator = null;
    int numLines = 0;
    int numEmptyLines = 0;

    try {
      iterator = FileUtils.lineIterator(file);

      while (iterator.hasNext()) {
        String line = iterator.nextLine();
        numLines++;
        if (StringUtils.isEmpty(line)) {
          numEmptyLines++;
        }
      }
    } catch (IOException e) {
      LOG.warn(e.getMessage());
    } finally {
      LineIterator.closeQuietly(iterator);
    }

    try {

      Log.debug("Count comment in " + file.getPath());

      int numCommentLines = new LineCountParser().countLinesOfComment(FileUtils.openInputStream(file));
      sensorContext.saveMeasure(xmlFile, CoreMetrics.LINES, (double) numLines);
      sensorContext.saveMeasure(xmlFile, CoreMetrics.COMMENT_LINES, (double) numCommentLines);
      sensorContext.saveMeasure(xmlFile, CoreMetrics.NCLOC, (double) numLines - numEmptyLines - numCommentLines);
    } catch (Exception e) {
      LOG.debug("Fail to count lines in " + file.getPath(), e);
    }

    LOG.debug("LineCountSensor: " + xmlFile.getKey() + ":" + numLines + "," + numEmptyLines + "," + 0);
  }

  public void analyse(Project project, SensorContext sensorContext) {

    for (InputFile inputfile : XmlPlugin.getFiles(project, settings)) {
      org.sonar.api.resources.File htmlFile = XmlProjectFileSystem.fromIOFile(inputfile, project);
      addMeasures(sensorContext, inputfile.getFile(), htmlFile);
    }
  }

  private boolean isEnabled(Project project) {
    return settings.getBoolean(CoreProperties.CORE_IMPORT_SOURCES_PROPERTY);
  }

  /**
   * This sensor only executes on XML projects.
   */
  public boolean shouldExecuteOnProject(Project project) {
    return isEnabled(project) && Xml.KEY.equals(project.getLanguageKey());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
