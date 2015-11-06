/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
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

import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.WildcardPattern;

/**
 * Abstract superclass for checks.
 *
 * @author Matthijs Galesloot
 */
public abstract class AbstractXmlCheck {

  private RuleKey ruleKey;
  private XmlSourceCode xmlSourceCode;

  protected final void createViolation(Integer linePosition, String message) {
    getWebSourceCode().addViolation(new XmlIssue(ruleKey, linePosition, message));
  }

  protected XmlSourceCode getWebSourceCode() {
    return xmlSourceCode;
  }

  /**
   * Check with ant style filepattern if the file is included.
   */
  protected boolean isFileIncluded(String filePattern) {
    if (filePattern != null) {
      return WildcardPattern.create(filePattern)
        .match(getWebSourceCode().getLogicalPath());

    } else {
      return true;
    }
  }

  public final void setRuleKey(RuleKey ruleKey) {
    this.ruleKey = ruleKey;
  }

  public RuleKey getRuleKey() {
    return ruleKey;
  }

  protected void setWebSourceCode(XmlSourceCode xmlSourceCode) {
    this.xmlSourceCode = xmlSourceCode;
  }

  public abstract void validate(XmlSourceCode xmlSourceCode);
}
