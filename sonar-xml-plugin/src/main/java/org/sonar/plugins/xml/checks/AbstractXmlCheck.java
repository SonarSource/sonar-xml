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

import org.apache.commons.lang.StringUtils;
import org.sonar.api.rules.Rule;
import org.sonar.api.utils.WildcardPattern;

/**
 * Abtract superclass for checks.
 *
 * @author Matthijs Galesloot
 */
public abstract class AbstractXmlCheck {

  private Rule rule;
  private XmlSourceCode xmlSourceCode;

  protected final void createViolation(Integer linePosition) {
    createViolation(linePosition, rule.getDescription());
  }

  protected final void createViolation(Integer linePosition, String message) {
    getWebSourceCode().addViolation(new XmlIssue(getWebSourceCode().getSonarFile(), rule.ruleKey(), linePosition, message));
  }

  protected XmlSourceCode getWebSourceCode() {
    return xmlSourceCode;
  }

  /**
   * Check with ant style filepattern if the file is included.
   */
  protected boolean isFileIncluded(String filePattern) {
    if (filePattern != null) {
      String fileName = getWebSourceCode().getSonarFile().getKey();
      WildcardPattern matcher = WildcardPattern.create(filePattern);
      return matcher.match(fileName);
    } else {
      return true;
    }
  }

  public final void setRule(Rule rule) {
    this.rule = rule;
  }

  protected void setWebSourceCode(XmlSourceCode xmlSourceCode) {
    this.xmlSourceCode = xmlSourceCode;
  }

  public String[] trimSplitCommaSeparatedList(String value) {
    String[] tokens = StringUtils.split(value, ",");
    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = tokens[i].trim();
    }
    return tokens;
  }

  public abstract void validate(XmlSourceCode xmlSourceCode);
}
