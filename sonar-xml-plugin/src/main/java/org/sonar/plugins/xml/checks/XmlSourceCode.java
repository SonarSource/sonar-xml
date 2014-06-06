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

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.xml.parsers.SaxParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks and analyzes report measurements, issues and other findings in WebSourceCode.
 *
 * @author Matthijs Galesloot
 */
public class XmlSourceCode {

  private static final Logger LOG = LoggerFactory.getLogger(XmlSourceCode.class);

  private final List<XmlIssue> xmlIssues = new ArrayList<XmlIssue>();

  private String code;

  private XmlFile xmlFile;
  private Document documentNamespaceAware = null;
  private Document documentNamespaceUnaware = null;

  public XmlSourceCode(org.sonar.api.resources.File sonarFile, File file) {
    this.xmlFile = new XmlFile(sonarFile, file);
  }

  public void addViolation(XmlIssue xmlIssue) {
    this.xmlIssues.add(xmlIssue);
  }

  InputStream createInputStream() {
    if (xmlFile.getIOFile() != null) {
      try {
        return FileUtils.openInputStream(xmlFile.getIOFile());
      } catch (IOException e) {
        throw new SonarException(e);
      }
    } else {
      return new ByteArrayInputStream(code.getBytes());
    }
  }

  protected Document getDocument(boolean namespaceAware) {
    return namespaceAware ? documentNamespaceAware : documentNamespaceUnaware;
  }

  public void parseSource(ModuleFileSystem fileSystem) {
    xmlFile.checkForCharactersBeforeProlog(fileSystem);

    documentNamespaceUnaware = parseFile(false);
    if (documentNamespaceUnaware != null) {
      documentNamespaceAware = parseFile(true);
    }
  }

  private Document parseFile(boolean namespaceAware) {
    return new SaxParser().parseDocument(xmlFile.getFilePath(), createInputStream(), namespaceAware);
  }

  public org.sonar.api.resources.File getSonarFile() {
    return xmlFile.getSonarFile();
  }

  public List<XmlIssue> getXmlIssues() {
    return xmlIssues;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public int getLineForNode(Node node) {
    return SaxParser.getLineNumber(node) + xmlFile.getLineDelta();
  }

  /**
   * Returns the line number where the prolog is located in the file.
   */
  public int getXMLPrologLine() {
    return xmlFile.getPrologLine();
  }

  public boolean isPrologFirstInSource() {
    return xmlFile.hasCharsBeforeProlog();
  }

  @Override
  public String toString() {
    return xmlFile.getSonarFile().getLongName();
  }
}
