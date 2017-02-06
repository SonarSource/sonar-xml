/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.checks;

import org.sonar.api.rule.RuleKey;

/**
 * Checks and analyzes report measurements, violations and other findings in WebSourceCode.
 *
 * @author Matthijs Galesloot
 */
public class XmlIssue {

  private final RuleKey ruleKey;
  private final Integer line;
  private final String message;

  public XmlIssue(RuleKey ruleKey, Integer line, String message) {
    this.ruleKey = ruleKey;
    this.line = line;
    this.message = message;
  }

  public RuleKey getRuleKey() {
    return ruleKey;
  }

  public Integer getLine() {
    return line;
  }

  public String getMessage() {
    return message;
  }
}
