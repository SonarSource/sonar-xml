/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonar.plugins.xml.checks;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Perform check for indenting of elements.
 *
 * @author Matthijs Galesloot
 */
@Rule(
  key = "IndentCheck",
  name = "Source code should be indented consistently",
  priority = Priority.MINOR,
  tags = {"convention"})
@BelongsToProfile(title = CheckRepository.SONAR_WAY_PROFILE_NAME, priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("1min")
public class IndentCheck extends AbstractXmlCheck {

  private static final String MESSAGE = "Make this line start at column %s.";

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

  /**
   * Collect the indenting whitespace before this node.
   */
  private int collectIndent(Node node) {
    int indent = 0;
    for (Node sibling = node.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
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

  /**
   * Get the depth of this node in the node hierarchy.
   */
  private static int getDepth(Node node) {
    int depth = 0;
    for (Node parent = node.getParentNode(); parent.getParentNode() != null; parent = parent.getParentNode()) {
      depth++;
    }
    return depth;
  }

  @Override
  public void validate(XmlSourceCode xmlSourceCode) {
    setWebSourceCode(xmlSourceCode);

    Document document = getWebSourceCode().getDocument(false);
    if (document.getDocumentElement() != null) {
      validateIndent(document.getDocumentElement());
    }
  }

  /**
   * Validate the indent for this node.
   */
  private boolean validateIndent(Node node) {

    int depth = getDepth(node);
    int indent = collectIndent(node);

    int expectedIndent = depth * indentSize;

    if (expectedIndent != indent) {
      createViolation(getWebSourceCode().getLineForNode(node), String.format(MESSAGE, expectedIndent + 1));
      return true;
    }

    // check the child elements

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
}
