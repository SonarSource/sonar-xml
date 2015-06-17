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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.parsers.LineCountParser;

/**
 * Count lines of code in XML files.
 *
 * @author Matthijs Galesloot
 */
public final class LineCountSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(LineCountSensor.class);
  private final FileSystem fileSystem;
  private final FilePredicate mainFilesPredicate;

  public LineCountSensor(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    this.mainFilesPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Xml.KEY));
  }

  private static void addMeasures(SensorContext sensorContext, InputFile inputFile) {

    LineIterator iterator = null;
    int numLines = 0;
    int numBlankLines = 0;

    try {
      iterator = FileUtils.lineIterator(inputFile.file());

      while (iterator.hasNext()) {
        String line = iterator.nextLine();
        numLines++;
        if (StringUtils.isBlank(line)) {
          numBlankLines++;
        }
      }
    } catch (IOException e) {
      LOG.warn("Unable to count lines for file: " + inputFile.file().getAbsolutePath());
      LOG.warn("Cause: {}", e);
    } finally {
      LineIterator.closeQuietly(iterator);
    }

    try {

      LOG.debug("Count comment in " + inputFile.file().getPath());

      LineCountParser lineCountParser = new LineCountParser();
      int numCommentLines = lineCountParser.countLinesOfComment(FileUtils.openInputStream(inputFile.file()));
      sensorContext.saveMeasure(inputFile, CoreMetrics.LINES, (double) numLines);
      sensorContext.saveMeasure(inputFile, CoreMetrics.COMMENT_LINES, (double) numCommentLines);
      sensorContext.saveMeasure(inputFile, CoreMetrics.NCLOC, (double) numLines - numBlankLines - numCommentLines);
    } catch (Exception e) {
      LOG.debug("Fail to count lines in " + inputFile.file().getPath(), e);
    }

    LOG.debug("LineCountSensor: " + inputFile.file().getName() + ":" + numLines + "," + numBlankLines + "," + 0);
  }

  @Override
  public void analyse(Project project, SensorContext sensorContext) {
    for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {
      addMeasures(sensorContext, inputFile);
    }
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.hasFiles(mainFilesPredicate);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
