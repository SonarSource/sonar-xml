/*
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
 * along with this program; if not, see https://www.sonarsource.com/legal/
 */
package org.sonar.plugins.xml.checks;

import java.util.Collections;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;

@Rule(key = CharBeforePrologCheck.RULE_KEY)
public class CharBeforePrologCheck extends SonarXmlCheck {

  public static final String RULE_KEY = "S1778";

  @Override
  public void scanFile(XmlFile file) {
    file.getPrologElement().ifPresent(prologElement -> {
      XmlTextRange prologStartLocation = prologElement.getPrologStartLocation();
      if (prologStartLocation.getStartLine() != 1 || prologStartLocation.getStartColumn() != 0) {
        reportIssue(prologStartLocation, "Remove all characters located before \"<?xml\".", Collections.emptyList());
      }
    });
  }

}
