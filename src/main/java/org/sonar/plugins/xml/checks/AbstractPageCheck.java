/*
 * Sonar Xml Plugin
 * Copyright (C) 2010 Matthijs Galesloot
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
import org.sonar.api.rules.Violation;

/**
 * Abtract superclass for checks.
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
public abstract class AbstractPageCheck {

  private Rule rule;
  private XmlSourceCode xmlSourceCode;

  protected XmlSourceCode getWebSourceCode() {
    return xmlSourceCode;
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

  protected final void createViolation(int linePosition) {
    createViolation(linePosition, rule.getDescription());
  }

  protected final void createViolation(int linePosition, String message) {
    Violation violation = Violation.create(rule, getWebSourceCode().getResource());
    violation.setMessage(message);
    violation.setLineId(linePosition);
    getWebSourceCode().addViolation(violation);
  }

  public final Rule getRule() {
    return rule;
  }

  public final String getRuleKey() {
    return rule.getConfigKey();
  }

  public final void setRule(Rule rule) {
    this.rule = rule;
  }

  public abstract void validate(XmlSourceCode xmlSourceCode);
}
