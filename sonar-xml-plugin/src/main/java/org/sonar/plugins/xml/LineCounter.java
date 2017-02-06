/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
