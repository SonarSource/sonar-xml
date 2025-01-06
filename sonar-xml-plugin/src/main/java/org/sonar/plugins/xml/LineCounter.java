/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.sonar.plugins.xml.Utils.splitLines;

public final class LineCounter {

  private static final Logger LOG = LoggerFactory.getLogger(LineCounter.class);

  private LineCounter() {
  }

  private static <T extends Serializable> void saveMeasure(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
    context.<T>newMeasure()
      .withValue(value)
      .forMetric(metric)
      .on(inputFile)
      .save();
  }

  public static void analyse(SensorContext context, FileLinesContextFactory fileLinesContextFactory, XmlFile xmlFile) {
    LOG.debug("Count lines in {}", xmlFile.getInputFile().uri());

    Set<Integer> linesOfCode = new HashSet<>();
    Set<Integer> commentLines = new HashSet<>();

    Document document = xmlFile.getDocument();
    visitNode(document, linesOfCode, commentLines);

    xmlFile.getPrologElement().ifPresent(prologElement ->
      addLinesRange(
        linesOfCode,
        prologElement.getPrologStartLocation().getStartLine(),
        prologElement.getPrologEndLocation().getEndLine()));

    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(xmlFile.getInputFile());
    linesOfCode.forEach(lineOfCode -> fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, lineOfCode, 1));
    fileLinesContext.save();

    saveMeasure(context, xmlFile.getInputFile(), CoreMetrics.COMMENT_LINES, commentLines.size());
    saveMeasure(context, xmlFile.getInputFile(), CoreMetrics.NCLOC, linesOfCode.size());

  }

  private static void visitNode(Node node, Set<Integer> linesOfCode, Set<Integer> commentLines) {
    XmlTextRange range = XmlFile.nodeLocation(node);

    switch (node.getNodeType()) {
      case Node.ELEMENT_NODE:
        // this will count attribute lines as well tag itself
        addLinesRange(linesOfCode, XmlFile.startLocation((Element) node));
        addLinesRange(linesOfCode, XmlFile.endLocation((Element) node));
        break;
      case Node.COMMENT_NODE:
        addNotEmptyLines(commentLines, node.getTextContent(), range);
        break;
      case Node.TEXT_NODE:
      case Node.CDATA_SECTION_NODE:
        addNotEmptyLines(linesOfCode, node.getTextContent(), range);
        break;
      case Node.DOCUMENT_TYPE_NODE:
        addLinesRange(linesOfCode, range);
        break;
      default:
        break;
    }

    XmlFile.children(node).forEach(child -> visitNode(child, linesOfCode, commentLines));
  }

  private static void addNotEmptyLines(Set<Integer> set, String text, XmlTextRange fullTextRange) {
    int lineNumber = fullTextRange.getStartLine();
    for (String line : splitLines(text)) {
      if (!line.trim().isEmpty()) {
        set.add(lineNumber);
      }
      lineNumber++;
    }
  }

  private static void addLinesRange(Set<Integer> set, int start, int end) {
    for (int line = start; line <= end; line++) {
      set.add(line);
    }
  }

  private static void addLinesRange(Set<Integer> set, XmlTextRange range) {
    addLinesRange(set, range.getStartLine(), range.getEndLine());
  }

}
