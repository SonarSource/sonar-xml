/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.xml.checks;

import java.util.Collections;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.xml.Utils;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;

/**
 * RSPEC-103
 */
@Rule(key = LineLengthCheck.RULE_KEY)
public class LineLengthCheck extends SonarXmlCheck {

  public static final String RULE_KEY = "S103";
  private static final int DEFAULT_LENGTH = 120;

  @RuleProperty(
    key = "maximumLineLength",
    description = "The maximum authorized line length",
    defaultValue = "" + DEFAULT_LENGTH,
    type = "INTEGER")
  private int maximumLineLength = DEFAULT_LENGTH;

  private static final Pattern RTRIM = Pattern.compile("\\s+$");

  public void setMaximumLineLength(int maximumLineLength) {
    this.maximumLineLength = maximumLineLength;
  }

  @Override
  public void scanFile(XmlFile file) {
    int lineNumber = 1;
    for (String line : Utils.splitLines(file.getContents())) {
      String trimLine = trimEndOfLine(line);
      int length = trimLine.length();
      if (length > maximumLineLength) {
        XmlTextRange textRange = new XmlTextRange(lineNumber, 0, lineNumber, trimLine.length());
        reportIssue(textRange,
          String.format("Split this %d characters long line (which is greater than %d authorized).", length, maximumLineLength),
          Collections.emptyList());
      }
      lineNumber++;
    }
  }

  private static String trimEndOfLine(String line) {
    return RTRIM.matcher(line).replaceAll("");
  }
}
