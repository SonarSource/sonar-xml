/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.plugins.xml.checks.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Rule(key = "S3423")
@DeprecatedRuleKey(repositoryKey = "java", ruleKey = "S3423")
public class PomElementOrderCheck extends SonarXmlCheck {

  private static final Comparator<Node> LINE_COMPARATOR = Comparator.comparingInt(n -> XmlFile.nodeLocation(n).getStartLine());

  private static final List<String> REQUIRED_ORDER = Arrays.asList(
    "modelVersion",
    "parent",
    "groupId",
    "artifactId",
    "version",
    "packaging",
    "name",
    "description",
    "url",
    "inceptionYear",
    "organization",
    "licenses",
    "developers",
    "contributors",
    "mailingLists",
    "prerequisites",
    "modules",
    "scm",
    "issueManagement",
    "ciManagement",
    "distributionManagement",
    "properties",
    "dependencyManagement",
    "dependencies",
    "repositories",
    "pluginRepositories",
    "build",
    "reporting",
    "profiles");

  @Override
  public void scanFile(XmlFile xmlFile) {
    if (!"pom.xml".equalsIgnoreCase(xmlFile.getInputFile().filename())) {
      return;
    }

    checkPositions(xmlFile.getDocument().getDocumentElement());
  }

  private static Optional<Element> getChildElementByName(String name, List<Node> children) {
    return children.stream()
      .filter(child -> child.getNodeType() == Node.ELEMENT_NODE)
      .map(Element.class::cast)
      .filter(element -> element.getTagName().equals(name))
      .findFirst();
  }

  private void checkPositions(Element project) {
    List<Node> children = XmlFile.children(project);
    List<Node> expectedOrder = REQUIRED_ORDER.stream()
      .map(elementName -> getChildElementByName(elementName, children))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());

    List<Node> observedOrder = expectedOrder.stream().sorted(LINE_COMPARATOR).collect(Collectors.toList());

    int lastWrongPosition = -1;
    int firstWrongPosition = -1;

    for (int index = 0; index < expectedOrder.size(); index++) {
      if (observedOrder.indexOf(expectedOrder.get(index)) != index) {
        lastWrongPosition = index;
        if (firstWrongPosition == -1) {
          firstWrongPosition = index;
        }
      }
    }

    if (lastWrongPosition == -1) {
      return;
    }

    List<Secondary> inconsistencies = new ArrayList<>();
    // only reports between first and last wrong position
    for (int index = firstWrongPosition; index <= lastWrongPosition; index++) {
      inconsistencies.add(new Secondary(expectedOrder.get(index), "Expected position: " + (index + 1)));
    }

    if (!inconsistencies.isEmpty()) {
      reportIssue(XmlFile.startLocation(project),
        "Reorder the elements of this pom to match the recommended order.", inconsistencies);
    }
  }
}
