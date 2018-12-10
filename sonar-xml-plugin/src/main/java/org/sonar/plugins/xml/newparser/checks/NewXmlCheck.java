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
package org.sonar.plugins.xml.newparser.checks;

import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.newparser.NewXmlFile;
import org.sonar.plugins.xml.newparser.XmlTextRange;
import org.w3c.dom.Node;

public abstract class NewXmlCheck {

  private SensorContext context;
  private InputFile inputFile;

  public final void scanFile(SensorContext context, NewXmlFile file) {
    this.context = context;
    this.inputFile = file.getInputFile();
    scanFile(file);
  }

  public abstract void scanFile(NewXmlFile file);

  public abstract String ruleKey();

  public final void reportIssueOnFile(String message, List<Integer> secondaryLocationLines) {
    NewIssue issue = context.newIssue();

    NewIssueLocation location = issue.newLocation()
      .on(inputFile)
      .message(message);

    for (Integer line : secondaryLocationLines) {
      NewIssueLocation secondary = issue.newLocation()
        .on(inputFile)
        .at(inputFile.selectLine(line));
      issue.addLocation(secondary);
    }

    issue
      .at(location)
      // FIXME reposirory is going to be variable in future.
      .forRule(RuleKey.of(Xml.REPOSITORY_KEY, ruleKey()))
      .save();
  }

  public final void reportIssue(XmlTextRange textRange, String message, List<XmlTextRange> secondaries) {
    NewIssue issue = context.newIssue();
    NewIssueLocation location = getLocation(textRange, issue).message(message);
    secondaries.forEach(secondary -> issue.addLocation(getLocation(secondary, issue)));

    issue
      .at(location)
      // FIXME reposirory is going to be variable in future.
      .forRule(RuleKey.of(Xml.REPOSITORY_KEY, ruleKey()))
      .save();
  }

  private NewIssueLocation getLocation(XmlTextRange textRange, NewIssue issue) {
    return issue.newLocation()
        .on(inputFile)
        .at(inputFile.newRange(
          textRange.getStartLine(),
          textRange.getStartColumn(),
          textRange.getEndLine(),
          textRange.getEndColumn()));
  }

  public final void reportIssue(Node node, String message) {
    XmlTextRange textRange = NewXmlFile.nodeLocation(node);
    reportIssue(textRange, message, Collections.emptyList());
  }
}
