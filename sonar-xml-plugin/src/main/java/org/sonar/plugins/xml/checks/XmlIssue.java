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

import org.sonar.api.resources.File;
import org.sonar.api.rules.Rule;

/**
 * Checks and analyzes report measurements, violations and other findings in WebSourceCode.
 *
 * @author Matthijs Galesloot
 */
public class XmlIssue {

  private final Rule rule;
  private final int line;
  private final String message;
  private final File file;

  public XmlIssue(File file, Rule rule, int line, String message) {
    this.file = file;
    this.rule = rule;
    this.line = line;
    this.message = message;
  }

  public Rule getRule() {
    return rule;
  }

  public int getLine() {
    return line;
  }

  public String getMessage() {
    return message;
  }
}
