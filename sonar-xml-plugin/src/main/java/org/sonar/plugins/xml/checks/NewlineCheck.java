/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2022 SonarSource SA
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
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.xml.Xml;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Rule(key = "S2321")
@DeprecatedRuleKey(ruleKey = "NewlineCheck", repositoryKey = Xml.REPOSITORY_KEY)
public class NewlineCheck extends SonarXmlCheck {

  private static final String MESSAGE_START = "Put this element on a separate line.";
  private static final String MESSAGE_END = "Add a newline after this tag.";

  @Override
  public void scanFile(XmlFile file) {
    visitNode(file.getDocument());
  }

  private void visitNode(Node node) {
    List<Node> children = XmlFile.children(node);

    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element currentElement = (Element) node;
      checkChildrenLine(children, currentElement);
      checkNextSiblingLine(currentElement);
    }

    children.forEach(this::visitNode);
  }

  private void checkChildrenLine(List<Node> children, Element currentElement) {
    getOutermostChildElements(children).ifPresent(outermostChildElements -> {
      XmlTextRange start = XmlFile.startLocation(currentElement);
      XmlTextRange end = XmlFile.endLocation(currentElement);

      XmlTextRange firstChildElementStart = XmlFile.startLocation(outermostChildElements.first);
      XmlTextRange lastChildElementEnd = XmlFile.endLocation(outermostChildElements.last);

      boolean firstChildBadlyFormatted = firstChildElementStart.getStartLine() == start.getEndLine();
      boolean lastChildBadlyFormatted = lastChildElementEnd.getEndLine() == end.getStartLine();

      boolean singleChildElement = outermostChildElements.last.equals(outermostChildElements.first);
      boolean singleLineChildElement = firstChildElementStart.getStartLine() == lastChildElementEnd.getEndLine();
      if (singleChildElement && singleLineChildElement && firstChildBadlyFormatted && lastChildBadlyFormatted) {
        // report once on the entire child element
        reportIssue(outermostChildElements.first, MESSAGE_START);
      } else {
        if (firstChildBadlyFormatted) {
          reportIssue(firstChildElementStart, MESSAGE_START, Collections.emptyList());
        }

        if (lastChildBadlyFormatted) {
          reportIssue(lastChildElementEnd, MESSAGE_END, Collections.emptyList());
        }
      }
    });
  }

  private void checkNextSiblingLine(Element node) {
    XmlTextRange end = XmlFile.endLocation(node);
    Element nextSiblingElement = getNextSiblingElement(node);
    if (nextSiblingElement != null) {
      XmlTextRange nextSiblingElementStart = XmlFile.startLocation(nextSiblingElement);
      if (nextSiblingElementStart.getStartLine() == end.getEndLine()) {
        reportIssue(nextSiblingElementStart, MESSAGE_START, Collections.emptyList());
      }
    }
  }

  @Nullable
  private static Element getNextSiblingElement(Node node) {
    Node nextSibling = node.getNextSibling();
    if (nextSibling == null) {
      return null;
    }
    if (nextSibling.getNodeType() == Node.ELEMENT_NODE) {
      return (Element) nextSibling;
    }

    return getNextSiblingElement(nextSibling);
  }

  private static Optional<OutermostChildElements> getOutermostChildElements(List<Node> children) {
    Element firstChildElement = null;
    Element lastChildElement = null;

    for (Node child : children) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        if (firstChildElement == null) {
          firstChildElement = (Element) child;
        }
        lastChildElement = (Element) child;
      }
    }

    if (firstChildElement != null) {
      return Optional.of(new OutermostChildElements(firstChildElement, lastChildElement));
    }
    return Optional.empty();
  }

  private static class OutermostChildElements {
    Element first = null;
    Element last = null;

    OutermostChildElements(Element first, Element last) {
      this.first = first;
      this.last = last;
    }
  }

}
