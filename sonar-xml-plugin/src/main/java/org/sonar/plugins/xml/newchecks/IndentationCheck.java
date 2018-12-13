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
import org.apache.commons.lang.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.xml.newparser.NewXmlFile;
import org.sonar.plugins.xml.newparser.XmlTextRange;
import org.sonar.plugins.xml.newparser.checks.NewXmlCheck;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * RSPEC-1120
 */
@Rule(key = IndentationCheck.RULE_KEY)
public class IndentationCheck extends NewXmlCheck {

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
  public void scanFile(NewXmlFile file) {
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

    return false;
  }

  private boolean checkIndentation(Element element) {
    int expectedIndent = depth(element) * indentSize;

    if (expectedIndent != startIndent(element)) {
      reportIssue(NewXmlFile.startLocation(element), expectedIndent);
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

  private int startIndent(Element element) {
    int indent = 0;
    for (Node sibling = element.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
      short nodeType = sibling.getNodeType();

      if (nodeType == Node.COMMENT_NODE || nodeType == Node.ELEMENT_NODE) {
        return indent;

      } else if (nodeType == Node.TEXT_NODE) {
        String text = sibling.getTextContent();
        if (!StringUtils.isWhitespace(text)) {
          // non whitespace found, we are done
          return indent;
        }
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
              break;
          }
        }
      }
    }
    return indent;
  }
}
