/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2019 SonarSource SA
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.xml.Utils;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;

/**
 * RSPEC-105
 */
@Rule(key = TabCharacterCheck.RULE_KEY)
public class TabCharacterCheck extends SonarXmlCheck {

  public static final String RULE_KEY = "IllegalTabCheck";

  @RuleProperty(key = "markAll", description = "Mark all tab errors", defaultValue = "false", type = "BOOLEAN")
  private boolean markAll;

  @Override
  public void scanFile(XmlFile file) {
    int lineNumber = 1;
    List<Integer> secondaries = new ArrayList<>();

    for (String line : Utils.splitLines(file.getContents())) {
      if (line.indexOf('\t') != -1) {
        secondaries.add(lineNumber);
        if (!markAll) {
          break;
        }
      }
      lineNumber++;
    }
    if (!secondaries.isEmpty()) {
      reportIssueOnFile("Replace all tab characters in this file by sequences of white-spaces.", markAll ? secondaries : Collections.emptyList());
    }
  }

  public void setMarkAll(boolean markAll) {
    this.markAll = markAll;
  }

}
