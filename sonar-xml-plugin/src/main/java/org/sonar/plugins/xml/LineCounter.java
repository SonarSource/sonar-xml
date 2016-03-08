/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
