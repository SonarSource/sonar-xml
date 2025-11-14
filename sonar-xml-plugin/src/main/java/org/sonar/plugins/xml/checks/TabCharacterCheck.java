/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.plugins.xml.Utils;
import org.sonar.plugins.xml.Xml;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;

/**
 * RSPEC-105
 */
@Rule(key = TabCharacterCheck.RULE_KEY)
@DeprecatedRuleKey(ruleKey = "IllegalTabCheck", repositoryKey = Xml.REPOSITORY_KEY)
public class TabCharacterCheck extends SonarXmlCheck {

  public static final String RULE_KEY = "S105";
  private static final Pattern TABS_REGEX = Pattern.compile("\t++");
  private static final int MAX_REPORTED_LOCATION = 21;

  @Override
  public void scanFile(XmlFile file) {
    String content = file.getContents();
    if (content.indexOf('\t') == -1) {
      return;
    }
    String[] lines = Utils.splitLines(content);
    List<XmlTextRange> firstTabLocations = new ArrayList<>();
    int extraTabsCount = 0;
    for (int lineNumber = 1; lineNumber <= lines.length; lineNumber++) {
      String line = lines[lineNumber - 1];
      Matcher tabsMatcher = TABS_REGEX.matcher(line);
      while (tabsMatcher.find()) {
        if (firstTabLocations.size() < MAX_REPORTED_LOCATION) {
          firstTabLocations.add(new XmlTextRange(lineNumber, tabsMatcher.start(), lineNumber, tabsMatcher.end()));
        } else {
          extraTabsCount += tabsMatcher.end() - tabsMatcher.start();
        }
      }
    }
    reportIssue(firstTabLocations, extraTabsCount);
  }

  private void reportIssue(List<XmlTextRange> firstTabLocations, int extraTabsCount) {
    XmlTextRange primaryLocation = firstTabLocations.get(0);
    List<Secondary> secondaries = new ArrayList<>();
    for (int i = 1; i < firstTabLocations.size(); i++) {
      XmlTextRange range = firstTabLocations.get(i);
      boolean limitReached = (i == MAX_REPORTED_LOCATION - 1);
      secondaries.add(new Secondary(range, "tab character(s)" +
        (limitReached && extraTabsCount > 0 ? (" (and " + extraTabsCount + " more in this file)") : "")));
    }
    reportIssue(primaryLocation, "Replace all tab characters in this file by sequences of white-spaces.", secondaries);
  }

}
