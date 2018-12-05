/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
package org.sonar.plugins.xml.checks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import static java.lang.String.format;

/**
 * Checks and analyzes report measurements, issues and other findings in WebSourceCode.
 *
 * @author Matthijs Galesloot
 */
public class XmlFile {

  private static final Logger LOG = Loggers.get(XmlFile.class);
  private static final String XML_PROLOG_START_TAG = "<?xml";
  public static final String BOM_CHAR = "\ufeff";

  private final InputFile inputFile;

  private File noCharBeforePrologFile;
  /**
   * Number of lines removed before xml prolog if present
   */
  private int lineDeltaForIssue = 0;
  /**
   * Number of characters removed before xml prolog if present
   */
  private int characterDeltaForHighlight = 0;
  private boolean hasCharsBeforeProlog = false;

  public XmlFile(InputFile inputFile, FileSystem fileSystem) {
    this.inputFile = inputFile;
    checkForCharactersBeforeProlog(fileSystem);
  }

  /**
   * Check if the xml file starts with a prolog "&lt?xml version="1.0" ?&gt"
   * if so, check if there is any characters prefixing it.
   */
  private void checkForCharactersBeforeProlog(FileSystem fileSystem) {
    try {
      int lineNb = 1;
      Pattern firstTagPattern = Pattern.compile("<[a-zA-Z?]+");
      boolean hasBOM = false;

      for (String line : inputFile.contents().split("\\r?\\n")) {
        if (lineNb == 1 && line.startsWith(BOM_CHAR)) {
          hasBOM = true;
          characterDeltaForHighlight = -1;
        }

        Matcher m = firstTagPattern.matcher(line);
        if (m.find()) {
          int column = line.indexOf(m.group());

          if (XML_PROLOG_START_TAG.equals(m.group()) && !isFileBeginning(lineNb, column, hasBOM)) {
            hasCharsBeforeProlog = true;
          }
          break;
        }
        lineNb++;
      }

      if (hasCharsBeforeProlog) {
        processCharBeforePrologInFile(fileSystem, lineNb);
      }
    } catch (IOException e) {
      LOG.warn(format("Unable to analyse file %s", inputFile.uri()), e);
    }
  }

  private static boolean isFileBeginning(int line, int column, boolean hasBOM) {
    return line == 1 && column <= (hasBOM ? 1 : 0);
  }

  /**
   * Create a temporary file without any character before the prolog and update the following
   * attributes in order to correctly report issues:
   * <ul>
   *   <li> lineDeltaForIssue
   *   <li> file
   */
  private void processCharBeforePrologInFile(FileSystem fileSystem, int lineDelta) {
    try {
      String content = inputFile.contents();
      File tempFile = new File(fileSystem.workDir(), inputFile.filename());

      int index = content.indexOf(XML_PROLOG_START_TAG);
      Files.write(tempFile.toPath(), content.substring(index).getBytes(inputFile.charset()));

      noCharBeforePrologFile = tempFile;

      if (index != -1) {
        characterDeltaForHighlight += index;
      }

      if (lineDelta > 1) {
        lineDeltaForIssue = lineDelta - 1;
      }

    } catch (IOException e) {
      LOG.warn("Unable to analyse file {}", inputFile.uri(), e);
    }
  }

  public InputFile getInputFile() {
    return inputFile;
  }

  public int getLineDelta() {
    return lineDeltaForIssue;
  }

  public int getOffsetDelta() {
    return characterDeltaForHighlight;
  }

  public InputStream getInputStream() throws IOException {
    if (noCharBeforePrologFile == null) {
      return inputFile.inputStream();
    }
    return Files.newInputStream(noCharBeforePrologFile.toPath());
  }

  public int getPrologLine() {
    return lineDeltaForIssue + 1;
  }

  public boolean hasCharsBeforeProlog() {
    return hasCharsBeforeProlog;
  }

  public String uri() {
    return inputFile.uri().toString();
  }

  public Charset getCharset() {
    return inputFile.charset();
  }
}
