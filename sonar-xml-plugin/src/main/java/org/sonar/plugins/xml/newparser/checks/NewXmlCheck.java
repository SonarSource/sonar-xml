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

import java.util.List;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.newparser.NewXmlFile;

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

  public final void reportIssueOnFile(String message, List<Integer> lines) {
    NewIssue issue = context.newIssue();

    NewIssueLocation location = issue.newLocation()
      .on(inputFile)
      .message(message);

    for (Integer line : lines) {
      NewIssueLocation secondary = issue.newLocation()
        .on(inputFile)
        .at(inputFile.selectLine(line));
      issue.addLocation(secondary);
    }

    issue
      .at(location)
      .forRule(RuleKey.of(CheckRepository.REPOSITORY_KEY, ruleKey()))
      .save();
  }
}
