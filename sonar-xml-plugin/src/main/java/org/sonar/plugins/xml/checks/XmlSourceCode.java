/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.xml.parsers.SaxParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Checks and analyzes report measurements, issues and other findings in WebSourceCode.
 *
 * @author Matthijs Galesloot
 */
public class XmlSourceCode {

  private final List<XmlIssue> xmlIssues = new ArrayList<>();

  private String code;

  private XmlFile xmlFile;
  private Document documentNamespaceAware = null;
  private Document documentNamespaceUnaware = null;

  public XmlSourceCode(XmlFile xmlFile) {
    this.xmlFile = xmlFile;
  }

  public void addViolation(XmlIssue xmlIssue) {
    this.xmlIssues.add(xmlIssue);
  }

  InputStream createInputStream() {
    if (xmlFile.getIOFile() != null) {
      try {
        return FileUtils.openInputStream(xmlFile.getIOFile());
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    } else {
      return new ByteArrayInputStream(code.getBytes());
    }
  }

  protected Document getDocument(boolean namespaceAware) {
    return namespaceAware ? documentNamespaceAware : documentNamespaceUnaware;
  }

  /**
   * Parses the source and returns true if succeeded false if the file is corrupted.
   */
  public boolean parseSource() {
    documentNamespaceUnaware = parseFile(false);
    if (documentNamespaceUnaware != null) {
      documentNamespaceAware = parseFile(true);
    }
    return documentNamespaceUnaware != null || documentNamespaceAware != null;
  }

  private Document parseFile(boolean namespaceAware) {
    return new SaxParser().parseDocument(xmlFile.getFilePath(), createInputStream(), namespaceAware);
  }

  public InputFile getInputFile() {
    return xmlFile.getInputFile();
  }

  public String getLogicalPath() {
    return xmlFile.getInputFile().absolutePath();
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
    return xmlFile.getInputFile().absolutePath();
  }
}
