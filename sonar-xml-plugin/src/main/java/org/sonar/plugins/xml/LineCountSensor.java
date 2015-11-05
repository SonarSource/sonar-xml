/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.resources.Project;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.parsers.LineCountParser;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * Count lines of code in XML files.
 *
 * @author Matthijs Galesloot
 */
public final class LineCountSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(LineCountSensor.class);
  private final FileSystem fileSystem;
  private final FilePredicate mainFilesPredicate;
  private final FileLinesContextFactory fileLinesContextFactory;

  public LineCountSensor(FileSystem fileSystem, FileLinesContextFactory fileLinesContextFactory) {
    this.fileSystem = fileSystem;
    this.mainFilesPredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage(Xml.KEY));
    this.fileLinesContextFactory = fileLinesContextFactory;
  }

  private void addMeasures(SensorContext sensorContext, InputFile inputFile, Charset encoding) throws IOException, SAXException {
    LOG.debug("Count lines in " + inputFile.file().getPath());

    LineCountParser lineCountParser = new LineCountParser(inputFile.file(), encoding);

    int linesNumber = lineCountParser.getLinesNumber();

    Set<Integer> effectiveCommentLines = lineCountParser.getEffectiveCommentLines();
    Set<Integer> linesOfCodeLines = lineCountParser.getLinesOfCodeLines();

    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(inputFile);

    for (int line = 1; line <= linesNumber; line++) {
      fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, linesOfCodeLines.contains(line) ? 1 : 0);
      fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, effectiveCommentLines.contains(line) ? 1 : 0);
    }

    fileLinesContext.save();

    sensorContext.saveMeasure(inputFile, CoreMetrics.LINES, (double) linesNumber);
    sensorContext.saveMeasure(inputFile, CoreMetrics.COMMENT_LINES, (double) effectiveCommentLines.size());
    sensorContext.saveMeasure(inputFile, CoreMetrics.NCLOC, (double) linesOfCodeLines.size());

  }

  @Override
  public void analyse(Project project, SensorContext sensorContext) {
    for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {
      try {
        addMeasures(sensorContext, inputFile, fileSystem.encoding());
      } catch (Exception e) {
        LOG.warn("Unable to count lines for file: " + inputFile.file().getAbsolutePath());
        LOG.warn("Cause: {}", e);
      }
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
