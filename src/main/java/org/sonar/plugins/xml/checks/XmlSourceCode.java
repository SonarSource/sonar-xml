/*
 * Sonar XML Plugin
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
import org.sonar.api.resources.Resource;
import org.sonar.api.rules.Violation;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.xml.parsers.SaxParser;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks and analyzes report measurements, violations and other findings in WebSourceCode.
 * 
 * @author Matthijs Galesloot
 */
public class XmlSourceCode {

  private String code;
  private final File file;

  private final Resource<?> resource;
  private final List<Violation> violations = new ArrayList<Violation>();

  private Document documentNamespaceAware;

  private Document documentNamespaceUnaware;

  public XmlSourceCode(Resource<?> resource, File file) {
    this.resource = resource;
    this.file = file;
  }

  public void addViolation(Violation violation) {
    this.violations.add(violation);
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

  protected Document getDocument(boolean namespaceAware) {
    InputStream inputStream = createInputStream();
    if (namespaceAware) {
      if (documentNamespaceAware == null) {
        documentNamespaceAware = new SaxParser().parseDocument(inputStream, true);
      }
      return documentNamespaceAware;
    } else {
      if (documentNamespaceUnaware == null) {
        documentNamespaceUnaware = new SaxParser().parseDocument(inputStream, false);
      }
      return documentNamespaceUnaware;
    }
  }

  public Resource<?> getResource() {
    return resource;
  }

  public List<Violation> getViolations() {
    return violations;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public File getFile() {
    return file;
  }

  @Override
  public String toString() {
    return resource.getLongName();
  }
}
