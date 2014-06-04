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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.xml.parsers.SaxParser;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks and analyzes report measurements, issues and other findings in WebSourceCode.
 *
 * @author Matthijs Galesloot
 */
public class XmlSourceCode {

  private static final Logger LOG = LoggerFactory.getLogger(XmlSourceCode.class);

  private final org.sonar.api.resources.File sonarFile;
  private final ModuleFileSystem fileSystem;
  private final List<XmlIssue> xmlIssues = new ArrayList<XmlIssue>();

  private String code;
  private File file;
  private boolean hasCharsBeforeProlog = false;

  private Document documentNamespaceAware = null;
  private Document documentNamespaceUnaware = null;

  public XmlSourceCode(org.sonar.api.resources.File sonarFile, File file, ModuleFileSystem fileSystem) {
    this.sonarFile = sonarFile;
    this.file = file;
    this.fileSystem = fileSystem;
  }

  public void addViolation(XmlIssue xmlIssue) {
    this.xmlIssues.add(xmlIssue);
  }

  InputStream createInputStream() {
    if (file != null) {
      try {
        return FileUtils.openInputStream(file);
      } catch (IOException e) {
        throw new SonarException(e);
      }
    } else {
      return new ByteArrayInputStream(code.getBytes());
    }
  }

  private String getFilePath() {
    return file != null ? file.getAbsolutePath() : null;
  }

  protected Document getDocument(boolean namespaceAware) {
    return namespaceAware ? documentNamespaceAware : documentNamespaceUnaware;
  }

  public void parseSource() {
    checkForCharactersBeforeProlog();

    documentNamespaceUnaware = parseFile(false);
    if (documentNamespaceUnaware != null) {
      documentNamespaceAware = parseFile(true);
    }
  }

  private Document parseFile(boolean namespaceAware) {
    return new SaxParser().parseDocument(getFilePath(), createInputStream(), namespaceAware);
  }

  private void checkForCharactersBeforeProlog() {
    if (file == null) {
      return;
    }

    try {
      int index = 0;
      Pattern firstTagPattern = Pattern.compile("<[a-zA-Z?]+");

      for (String line : Files.readLines(file, fileSystem.sourceCharset())) {
        Matcher m = firstTagPattern.matcher(line);

        if (m.find()) {
          if ("<?xml".equals(m.group())) {
            index = line.indexOf(m.group());
            hasCharsBeforeProlog = true;
          }
          break;
        }
      }
      if (index > 0) {
        setFileToTemporaryFileWithoutChars(index);
      }
    } catch (IOException e) {
      LOG.warn(e.getMessage());
    }
  }

  private void setFileToTemporaryFileWithoutChars(int index) throws IOException {
    File tempFile = new File(fileSystem.workingDir(), file.getName());
    String content = Files.toString(file, fileSystem.sourceCharset()).substring(index);

    Files.write(content, tempFile, fileSystem.sourceCharset());
    file = tempFile;
  }

  public org.sonar.api.resources.File getSonarFile() {
    return sonarFile;
  }

  public List<XmlIssue> getXmlIssues() {
    return xmlIssues;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public boolean hasCharsBeforeProlog() {
    return hasCharsBeforeProlog;
  }

  @Override
  public String toString() {
    return sonarFile.getLongName();
  }
}
