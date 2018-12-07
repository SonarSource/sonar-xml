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
package org.sonar.plugins.xml.newchecks;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.plugins.xml.newparser.NewXmlFile;
import org.sonar.plugins.xml.newparser.XmlTextRange;
import org.sonar.plugins.xml.newparser.checks.NewXmlCheck;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Rule(key = NewlineCheck.RULE_KEY)
public class NewlineCheck extends NewXmlCheck {

  public static final String RULE_KEY = "NewlineCheck";

  private static final String MESSAGE_START = "Put this element on a separate line.";
  private static final String MESSAGE_END = "Add a newline after this tag.";

  @Override
  public String ruleKey() {
    return RULE_KEY;
  }

  @Override
  public void scanFile(NewXmlFile file) {
    visitNode(file.getDocument());
  }

  private void visitNode(Node node) {
    List<Node> children = NewXmlFile.children(node);

    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element currentElement = (Element) node;
      checkChildrenLine(children, currentElement);
      checkNextSiblingLine(currentElement);
    }

    children.forEach(this::visitNode);
  }

  private void checkChildrenLine(List<Node> children, Element currentElement) {
    OutermostChildElements outermostChildElements = getOutermostChildElements(children);
    XmlTextRange reportedRange = null;

    if (outermostChildElements.first != null) {
      XmlTextRange start = NewXmlFile.startLocation(currentElement);
      XmlTextRange firstChildElementStart = NewXmlFile.startLocation(outermostChildElements.first);
      if (firstChildElementStart.getStartLine() == start.getEndLine()) {
        reportedRange = firstChildElementStart;
      }
    }

    if (outermostChildElements.last != null) {
      XmlTextRange end = NewXmlFile.endLocation(currentElement);
      XmlTextRange lastChildElementRange = NewXmlFile.nodeLocation(outermostChildElements.last);
      XmlTextRange lastChildElementEnd = NewXmlFile.endLocation(outermostChildElements.last);
      if (lastChildElementEnd.getEndLine() == end.getStartLine()) {
        boolean reportEntireElement = outermostChildElements.last.equals(outermostChildElements.first)
          && reportedRange != null
          && lastChildElementRange.getStartLine() == lastChildElementRange.getEndLine();
        if (reportEntireElement) {
          reportedRange = lastChildElementRange;
        } else {
          reportIssue(lastChildElementEnd, MESSAGE_END, Collections.emptyList());
        }
      }
    }

    if (reportedRange != null) {
      reportIssue(reportedRange, MESSAGE_START, Collections.emptyList());
    }
  }

  private void checkNextSiblingLine(Element node) {
    XmlTextRange end = NewXmlFile.endLocation(node);
    Element nextSiblingElement = getNextSiblingElement(node);
    if (nextSiblingElement != null){
      XmlTextRange nextSiblingElementStart = NewXmlFile.startLocation(nextSiblingElement);
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

  private static OutermostChildElements getOutermostChildElements(List<Node> children) {
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

    return new OutermostChildElements(firstChildElement, lastChildElement);
  }

  private static class OutermostChildElements {
    Element first = null;
    Element last = null;

    OutermostChildElements(@Nullable Element first, @Nullable Element last) {
      this.first = first;
      this.last = last;
    }
  }

}
