/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
package org.sonar.plugins.xml.checks;

import java.util.Collections;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.xml.Xml;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import static org.sonar.plugins.xml.Utils.isSelfClosing;

@Rule(key = "S1120")
@DeprecatedRuleKey(ruleKey = "IndentCheck", repositoryKey = Xml.REPOSITORY_KEY)
public class IndentationCheck extends SonarXmlCheck {

  private static final String MESSAGE = "Make this line start after %d spaces to indent the code consistently.";

  @RuleProperty(
    key = "indentSize",
    description = "Number of white spaces of an indent. If this property is not set, we just check that the code is indented.",
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
    reportIssue(textRange, String.format(MESSAGE, expectedIndent), Collections.emptyList());
  }

  private static int depth(Node node) {
    int depth = 0;
    for (Node parent = node.getParentNode(); parent.getParentNode() != null; parent = parent.getParentNode()) {
      depth++;
    }
    return depth;
  }

  private int textFirstLineIndent(String text) {
    int indent = 0;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch (c) {
        case '\t':
          // add tabsize
          indent += tabSize;
          break;
        case ' ':
          // add one space
          indent++;
          break;
        default:
          // any other character, return current indent
          return indent;
      }
    }
    return indent;
  }

  private int startIndent(Node node) {
    int indent = 0;
    for (Node sibling = node; sibling != null; sibling = sibling.getPreviousSibling()) {
      short nodeType = sibling.getNodeType();

      if (nodeType == Node.COMMENT_NODE || nodeType == Node.ELEMENT_NODE) {
        return indent;

      } else if (nodeType == Node.TEXT_NODE) {
        String text = sibling.getTextContent();
        return textFirstLineIndent(text.lines().reduce((first, second) -> second).orElse(""));
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

      // @formatter:off
      // Special case: if element contains text that continues on the closing tag line, we do not report an issue
      // Valid examples:
      // <tag>Some text
      // that continues on the next line</tag>
      // <tag>
      // Some text
      // that continues on the next line</tag>
      // However if the last line before the closing tag is empty or contains only whitespace, we keep reporting an issue
      // Invalid example:
      // <tag>
      // Some text
      // </tag>
      // @formatter:on
      boolean isTextContent = element.getChildNodes().getLength() == 1 && element.getFirstChild() instanceof Text;

      if (isTextContent) {
        checkTextContent(element);
        return;
      }

      int startIndent = startIndent(element.getPreviousSibling());
      int endIndent = startIndent(element.getLastChild());
      if (startIndent != endIndent) {
        reportIssue(endLocation, startIndent);
      }
    }
  }

  private void checkTextContent(Element element) {
    // two options :
    // - line continuation
    // - multiline string
    String stringVal = Optional.ofNullable(element.getFirstChild().getNodeValue()).orElse("");
    String firstLine = stringVal.lines().reduce((first, second) -> first).orElse("");
    boolean isLineContinuation = !firstLine.trim().isEmpty();
    if (isLineContinuation) {
      checkLineContinuation(element, stringVal);
    } else {
      checkMultilineText(element, stringVal);
    }
  }

  private void checkLineContinuation(Element element, String stringVal) {
    // verify that each line is indented correctly
    // it means 1 indent level more than the parent element
    int expectedIndent = (depth(element) + 1) * indentSize;
    int[] lineLoc = {XmlFile.startLocation(element).getStartLine()};
    stringVal
      .lines()
      .skip(1) // first line is after opening tag
      .forEach(line -> {
        lineLoc[0]++;
        if (line.trim().isEmpty()) {
          return;
        }
        int actualIndent = textFirstLineIndent(line);
        if (actualIndent != expectedIndent) {
          XmlTextRange location = new XmlTextRange(
            lineLoc[0],
            actualIndent,
            lineLoc[0],
            actualIndent + line.trim().length());
          reportIssue(location, expectedIndent);
        }
      });
    checkTextContentClosingTag(element, stringVal, lineLoc[0]);
  }

  private void checkMultilineText(Element element, String stringVal) {
    // only check that last line is empty
    int linesCount = (int) stringVal.lines().count();
    String lastLine = stringVal.lines().reduce((first, second) -> second).orElse("");
    int line = XmlFile.startLocation(element).getStartLine() + linesCount - 1;
    if (linesCount > 1 && !lastLine.trim().isEmpty()) {
      int expectedIndent = (depth(element) + 1) * indentSize;
      int actualIndent = textFirstLineIndent(lastLine);
      if (actualIndent != expectedIndent) {
        XmlTextRange location = new XmlTextRange(
          line,
          actualIndent,
          line,
          actualIndent + lastLine.trim().length());
        reportIssue(location, expectedIndent);
      }
    }
    checkTextContentClosingTag(element, stringVal, line);
  }

  private void checkTextContentClosingTag(Element element, String stringVal, int line) {
    // check closing tag indentation if on a new line
    boolean lastLineEmpty = stringVal
      .lines()
      .reduce((first, second) -> second)
      .orElse("")
      .trim()
      .isEmpty();
    if (lastLineEmpty) {
      int actualIndent = startIndent(element.getLastChild());
      if (actualIndent != depth(element) * indentSize) {
        XmlTextRange location = new XmlTextRange(
          line,
          actualIndent,
          line,
          actualIndent + element.getTagName().length() + 3); // +3 for </ and >
        reportIssue(location, depth(element) * indentSize);
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
