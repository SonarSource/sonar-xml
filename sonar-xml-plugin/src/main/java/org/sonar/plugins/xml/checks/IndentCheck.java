/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
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
import org.sonar.check.BelongsToProfile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Perform check for indenting of elements.
 *
 * @author Matthijs Galesloot
 */
@Rule(key = "IndentCheck",
  priority = Priority.MINOR)
@BelongsToProfile(title = CheckRepository.SONAR_WAY_PROFILE_NAME, priority = Priority.MINOR)
public class IndentCheck extends AbstractXmlCheck {

  @RuleProperty(key = "indentSize", defaultValue = "2")
  private int indentSize = 2;

  @RuleProperty(key = "tabSize", defaultValue = "2")
  private int tabSize = 2;

  /**
   * Collect the indenting whitespace before this node.
   */
  private int collectIndent(Node node) {
    int indent = 0;
    for (Node sibling = node.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
      switch (sibling.getNodeType()) {
        case Node.COMMENT_NODE:
        case Node.ELEMENT_NODE:
          return indent;
        case Node.TEXT_NODE:
          String text = sibling.getTextContent();
          if (StringUtils.isWhitespace(text)) {
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
          } else {
            // non whitespace found, we are done
            return indent;
          }
          break;
        default:
          break;
      }
    }
    return indent;
  }

  /**
   * Get the depth of this node in the node hierarchy.
   */
  private int getDepth(Node node) {
    int depth = 0;
    for (Node parent = node.getParentNode(); parent.getParentNode() != null; parent = parent.getParentNode()) {
      depth++;
    }
    return depth;
  }

  public int getIndentSize() {
    return indentSize;
  }

  public int getTabSize() {
    return tabSize;
  }

  public void setIndentSize(int indentSize) {
    this.indentSize = indentSize;
  }

  public void setTabSize(int tabSize) {
    this.tabSize = tabSize;
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
  private void validateIndent(Node node) {

    int depth = getDepth(node);
    int indent = collectIndent(node);

    if (depth * indentSize != indent) {
      createViolation(getWebSourceCode().getLineForNode(node), "Wrong indentation");
    }

    // check the child elements
    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      switch (child.getNodeType()) {
        case Node.ELEMENT_NODE:
        case Node.COMMENT_NODE:
          validateIndent(child);
          break;
        default:
          break;
      }
    }
  }
}
