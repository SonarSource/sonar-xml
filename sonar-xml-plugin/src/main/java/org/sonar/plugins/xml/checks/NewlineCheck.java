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
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Perform check for newline after elements.
 *
 * @author Matthijs Galesloot
 */
@Rule(key = "NewlineCheck",
  name = "Newlines should follow each element",
  priority = Priority.MINOR)
@BelongsToProfile(title = CheckRepository.SONAR_WAY_PROFILE_NAME, priority = Priority.MINOR)
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.READABILITY)
@SqaleConstantRemediation("2min")
public class NewlineCheck extends AbstractXmlCheck {

  /**
   * Validate newlines for node.
   */
  private void validateNewline(Node node) {

    // check if we have a newline after the elements and after each childelement.
    boolean newline = false;
    Node lastChild = null;

    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      short nodeType = child.getNodeType();

      if (nodeType == Node.COMMENT_NODE) {
        lastChild = child;

      } else if (nodeType == Node.ELEMENT_NODE) {
        // check if there is a new node before we have had any newlines.
        if (!newline) {
          createViolation(getWebSourceCode().getLineForNode(child), "Node should be on the next line");
        } else {
          newline = false;
        }
        lastChild = child;

      } else if (nodeType == Node.TEXT_NODE) {
        // newline check is OK if there is non whitespace or the whitespace contains a newline
        String textContent = child.getTextContent();
        if (!StringUtils.isWhitespace(textContent) || textContent.contains("\n")) {
          newline = true;
        }
      }
    }

    // validate first last child.
    validateLastChild(newline, lastChild);

    // check the child elements
    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        validateNewline(child);
      }
    }
  }

  private void validateLastChild(boolean newlineAfterLastChild, Node lastChild) {
    if (!newlineAfterLastChild && lastChild != null) {
      createViolation(getWebSourceCode().getLineForNode(lastChild), "Missing newline after last element");
    }
  }

  @Override
  public void validate(XmlSourceCode xmlSourceCode) {
    setWebSourceCode(xmlSourceCode);

    Document document = getWebSourceCode().getDocument(false);
    if (document.getDocumentElement() != null) {
      validateNewline(document.getDocumentElement());
    }
  }
}
