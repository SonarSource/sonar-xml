/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml.checks;

import org.sonar.check.Rule;

/**
 * RSPEC-1134
 */
@Rule(key = FixmeCommentCheck.RULE_KEY)
public class FixmeCommentCheck extends CommentContainsPatternChecker {

  public static final String RULE_KEY = "S1134";
  private static final String PATTERN = "FIXME";
  private static final String MESSAGE = "Take the required action to fix the issue indicated by this \"FIXME\" comment.";

  public FixmeCommentCheck() {
    super(PATTERN, MESSAGE);
  }

}
