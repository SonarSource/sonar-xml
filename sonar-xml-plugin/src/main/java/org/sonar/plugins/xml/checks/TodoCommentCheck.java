/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
 * RSPEC-1135
 */
@Rule(key = TodoCommentCheck.RULE_KEY)
public class TodoCommentCheck extends CommentContainsPatternChecker {

  public static final String RULE_KEY = "S1135";
  private static final String PATTERN = "TODO";
  private static final String MESSAGE = "Complete the task associated to this \"TODO\" comment.";

  public TodoCommentCheck() {
    super(PATTERN, MESSAGE);
  }

}
