/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
