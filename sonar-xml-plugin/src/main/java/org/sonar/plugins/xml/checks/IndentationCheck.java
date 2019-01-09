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

import java.util.Collections;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.sonar.plugins.xml.Utils.isSelfClosing;

/**
 * RSPEC-1120
 */
@Rule(key = IndentationCheck.RULE_KEY)
public class IndentationCheck extends SonarXmlCheck {

  public static final String RULE_KEY = "IndentCheck";

  private static final String MESSAGE = "Make this element start at column %s.";

  @RuleProperty(
    key = "indentSize",
    description = "Number of white-spaces of an indent. If this property is not set, we just check that the code is indented.",
    defaultValue = "2",
    type = "INTEGER")
  private int indentSize = 2;

  @RuleProperty(
    key = "tabSize",
    description = "Equivalent number of spaces of a tabulation",
    defaultValue = "2",
    type = "INTEGER")
  private int tabSize = 2;

  @Override
  public void scanFile(XmlFile file) {
    validateIndent(file.getDocument());
  }

  public void setIndentSize(int indentSize) {
    this.indentSize = indentSize;
  }

  public void setTabSize(int tabSize) {
    this.tabSize = tabSize;
  }

  private boolean validateIndent(Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE && checkIndentation((Element) node)) {
      return true;
    }

    boolean issueOnLine = false;

    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      switch (child.getNodeType()) {
        case Node.ELEMENT_NODE:
          if (!issueOnLine) {
            issueOnLine = validateIndent(child);
          }
          break;
        case Node.TEXT_NODE:
          if (child.getTextContent().contains("\n")) {
            issueOnLine = false;
          }
          break;
        case Node.COMMENT_NODE:
        default:
          break;
      }
    }

    // Check indentation of closing tag
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      checkClosingTag((Element) node);
    }

    return false;
  }

  private boolean checkIndentation(Element element) {
    if (!needToCheckIndentation(element)) {
      return false;
    }

    int expectedIndent = depth(element) * indentSize;
    if (expectedIndent != startIndent(element.getPreviousSibling())) {
      reportIssue(XmlFile.startLocation(element), expectedIndent);
      // if reporting on start node, don't report on rest of the block
      return true;
    }
    return false;
  }

  private void reportIssue(XmlTextRange textRange, int expectedIndent) {
    reportIssue(textRange, String.format(MESSAGE, expectedIndent + 1), Collections.emptyList());
  }

  private static int depth(Node node) {
    int depth = 0;
    for (Node parent = node.getParentNode(); parent.getParentNode() != null; parent = parent.getParentNode()) {
      depth++;
    }
    return depth;
  }

  private int startIndent(Node node) {
    int indent = 0;
    for (Node sibling = node; sibling != null; sibling = sibling.getPreviousSibling()) {
      short nodeType = sibling.getNodeType();

      if (nodeType == Node.COMMENT_NODE || nodeType == Node.ELEMENT_NODE) {
        return indent;

      } else if (nodeType == Node.TEXT_NODE) {
        String text = sibling.getTextContent();
        for (int i = text.length() - 1; i >= 0; i--) {
          char c = text.charAt(i);
          switch (c) {
            case '\n':
              // newline found, we are done
              return indent;
            case '\t':
              // add tabsize
              indent += tabSize;
              break;
            case ' ':
              // add one space
              indent++;
              break;
            default:
              return indent;
          }
        }
      }
    }
    return indent;
  }

  private void checkClosingTag(Element element) {
    if (isSelfClosing(element)) {
      return;
    }
    XmlTextRange startLocation = XmlFile.startLocation(element);
    XmlTextRange endLocation = XmlFile.endLocation(element);
    if (startLocation.getEndLine() != endLocation.getStartLine()) {
      if (!needToCheckIndentation(element)) {
        return;
      }
      int startIndent = startIndent(element.getPreviousSibling());
      int endIndent = startIndent(element.getLastChild());
      if (startIndent != endIndent) {
        reportIssue(endLocation, startIndent);
      }
    }
  }

  private static boolean needToCheckIndentation(Element element) {
    // When tag is inside lines of text we do not check indentation

    if (element.getChildNodes().getLength() > 1) {
      return true;
    }

    Node previous = element.getPreviousSibling();
    if (isNonEmptyTextNode(previous)) {
      return false;
    }

    Node next = element.getNextSibling();
    if (isNonEmptyTextNode(next)) {
      return false;
    }

    // Current tag can be encapsulated in another tag which is inside lines of text
    for (Node parent = element.getParentNode(); parent != null && parent.getChildNodes().getLength() == 1; parent = parent.getParentNode()) {
      short parentType = parent.getNodeType();
      if (parentType == Node.ELEMENT_NODE && isNonEmptyTextNode(parent.getPreviousSibling())) {
        return false;
      }
    }
    return true;
  }

  private static boolean isNonEmptyTextNode(@Nullable Node node) {
    return node != null
      && node.getNodeType() == Node.TEXT_NODE
      && !node.getTextContent().trim().isEmpty();
  }
  
}
