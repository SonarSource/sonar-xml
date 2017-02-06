/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.checks;

import javax.annotation.Nullable;
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
  protected boolean isFileIncluded(@Nullable String filePattern) {
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
