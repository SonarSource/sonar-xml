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
package org.sonar.plugins.xml.checks;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks and analyzes report measurements, issues and other findings in WebSourceCode.
 *
 * @author Matthijs Galesloot
 */
public class XmlFile {

  private static final Logger LOG = LoggerFactory.getLogger(XmlFile.class);


  private File file;
  private final File originalFile;
  private final org.sonar.api.resources.File sonarFile;

  /**
   * Number of lines removed before xml prolog if present
   */
  private int lineDeltaForIssue = 0;
  private boolean hasCharsBeforeProlog = false;
  private final static String XML_PROLOG_START_TAG = "<?xml";

  public XmlFile(org.sonar.api.resources.File sonarFile, File file) {
    this.sonarFile = sonarFile;
    this.file = file;
    this.originalFile = file;
  }

  public String getFilePath() {
    return originalFile != null ? originalFile.getAbsolutePath() : null;
  }

  /**
   * Check if the xml file starts with a prolog "&lt?xml version="1.0" ?&gt"
   * if so, check if there is any characters prefixing it.
   */
  public void checkForCharactersBeforeProlog(ModuleFileSystem fileSystem) {
    if (file == null) {
      return;
    }

    try {
      int lineNb = 1;
      Pattern firstTagPattern = Pattern.compile("<[a-zA-Z?]+");

      for (String line : Files.readLines(file, fileSystem.sourceCharset())) {
        Matcher m = firstTagPattern.matcher(line);
        if (m.find()) {
          int groupIndex = line.indexOf(m.group());

          if (XML_PROLOG_START_TAG.equals(m.group()) && (groupIndex > 0 || lineNb > 1)) {
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
      LOG.warn(e.getMessage());
    }
  }

  /**
   * Create a temporary file without any character before the prolog and update the following
   * attributes in order to correctly report issues:
   * <ul>
   *   <li> lineDeltaForIssue
   *   <li> file
   */
  private void processCharBeforePrologInFile(ModuleFileSystem fileSystem, int lineDelta) {
    try {
      String content = Files.toString(file, fileSystem.sourceCharset());
      File tempFile = new File(fileSystem.workingDir(), file.getName());

      int index = content.indexOf(XML_PROLOG_START_TAG);
      Files.write(content.substring(index), tempFile, fileSystem.sourceCharset());

      file = tempFile;
      if (lineDelta > 1) {
        lineDeltaForIssue = lineDelta - 1;
      }

    } catch (IOException e) {
      LOG.warn(e.getMessage());
    }
  }

  public org.sonar.api.resources.File getSonarFile() {
    return sonarFile;
  }

  public int getLineDelta() {
    return lineDeltaForIssue;
  }

  public File getIOFile() {
    return file;
  }

  public int getPrologLine() {
    return lineDeltaForIssue + 1;
  }

  public boolean hasCharsBeforeProlog() {
    return hasCharsBeforeProlog;
  }
}
