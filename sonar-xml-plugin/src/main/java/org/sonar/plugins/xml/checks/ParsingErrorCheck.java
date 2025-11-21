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
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;

@Rule(key = ParsingErrorCheck.RULE_KEY)
public class ParsingErrorCheck extends SonarXmlCheck {

  public static final String RULE_KEY = "S2260";

  @Override
  public void scanFile(XmlFile file) {
    // we don't do anything here. Having the XML file parsable (well formed) is a precondition
    // for all rules and thus checked up front. The rule however still need to be registered in SQ
    // and 'physically' exist as class
  }
}
