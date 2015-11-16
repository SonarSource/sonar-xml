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
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.plugins.xml.checks.XmlFile;
import org.sonar.plugins.xml.parsers.LineCountParser;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Count lines of code in XML files.
 *
 * @author Matthijs Galesloot
 */
public final class LineCounter {

  private static final Logger LOG = LoggerFactory.getLogger(LineCounter.class);

  private LineCounter() {
  }

  private static void saveMeasures(XmlFile xmlFile, LineCountData data, FileLinesContext fileLinesContext, SensorContext context) throws IOException, SAXException {
    data.updateAccordingTo(xmlFile.getLineDelta());

    for (int line = 1; line <= data.linesNumber(); line++) {
      fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, data.linesOfCodeLines().contains(line) ? 1 : 0);
      fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, data.effectiveCommentLines().contains(line) ? 1 : 0);
    }
    fileLinesContext.save();

    context.saveMeasure(xmlFile.getInputFile(), CoreMetrics.LINES, (double) data.linesNumber());
    context.saveMeasure(xmlFile.getInputFile(), CoreMetrics.COMMENT_LINES, (double) data.effectiveCommentLines().size());
    context.saveMeasure(xmlFile.getInputFile(), CoreMetrics.NCLOC, (double) data.linesOfCodeLines().size());
  }

  public static void analyse(SensorContext context, FileLinesContextFactory fileLinesContextFactory, XmlFile xmlFile, Charset encoding) {
    LOG.debug("Count lines in " + xmlFile.getIOFile().getPath());

    try {
      saveMeasures(
        xmlFile,
        new LineCountParser(xmlFile.getIOFile(), encoding).getLineCountData(),
        fileLinesContextFactory.createFor(xmlFile.getInputFile()), context);

    } catch (Exception e) {
      LOG.warn("Unable to count lines for file: " + xmlFile.getIOFile().getAbsolutePath());
      LOG.warn("Cause: ", e);
    }
  }

}
