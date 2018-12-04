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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.ParserConfigurationException;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.Issue.Flow;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.plugins.xml.newparser.NewXmlFile;

import static org.assertj.core.api.Assertions.assertThat;

public class NewXmlVerifier {
  private static final Path BASE_DIR = Paths.get("src/test/resources/checks/");

  private final Collection<Issue> issues;

  private NewXmlVerifier(Collection<Issue> issues) {
    this.issues = issues;
  }

  public static void verifyIssueOnFile(String relativePath, NewXmlCheck check, String expectedIssueMessage, int... secondaryLines) {
    createVerifier(relativePath, check).checkIssueOnFile(expectedIssueMessage, secondaryLines);
  }

  public static void verifyNoIssue(String relativePath, NewXmlCheck check) {
    createVerifier(relativePath, check).checkNoIssues();
  }

  private static NewXmlVerifier createVerifier(String fileName, NewXmlCheck check) {
    File file = new File(BASE_DIR.toFile(), check.getClass().getSimpleName() + "/" + fileName);

    RuleKey checkRuleKey = RuleKey.of(Xml.REPOSITORY_KEY, check.ruleKey());

    SensorContextTester context = SensorContextTester.create(BASE_DIR)
      .setActiveRules(new ActiveRulesBuilder().create(checkRuleKey).activate().build());

    String filePath = file.getPath();
    String content;
    try {
      content = Files.lines(file.toPath()).collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Unable to load content of file %s", filePath), e);
    }

    DefaultInputFile defaultInputFile = TestInputFileBuilder.create("", filePath)
      .setType(InputFile.Type.MAIN)
      .initMetadata(content)
      .setLanguage(Xml.KEY)
      .setCharset(StandardCharsets.UTF_8)
      .build();

    context.fileSystem().add(defaultInputFile);

    NewXmlFile newXmlFile;
    try {
      newXmlFile = NewXmlFile.create(defaultInputFile);
    } catch (IOException e) {
      throw new IllegalStateException(String.format("Unable to scan xml file %s", filePath), e);
    }

    check.scanFile(context, newXmlFile);
    return new NewXmlVerifier(context.allIssues());
  }

  private void checkIssueOnFile(String expectedIssueMessage, int... secondaryLines) {
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo(expectedIssueMessage);
    List<Flow> flows = issue.flows();
    if (secondaryLines == null) {
      assertThat(flows).hasSize(1);
      assertThat(flows.get(0).locations()).isEmpty();
    } else {
      // secondaries are N flows of size 1
      assertThat(flows).hasSize(secondaryLines.length);
      assertThat(flows.stream().map(Flow::locations)).allMatch(flow -> flow.size() == 1);

      // only contains lines
      Integer[] expectedLines = IntStream.of(secondaryLines).boxed().toArray(Integer[]::new);
      assertThat(flows.stream().map(Flow::locations).map(locs -> locs.get(0).textRange().start().line()).collect(Collectors.toList()))
        .containsExactly(expectedLines);
    }
  }

  private void checkNoIssues() {
    assertThat(issues).isEmpty();
  }
}
