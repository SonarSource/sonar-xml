/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.checks;

import javax.annotation.Nullable;
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

    checkChildElements(node);
  }

  private void checkChildElements(Node node) {
    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        validateNewline(child);
      }
    }
  }

  private void validateLastChild(boolean newlineAfterLastChild, @Nullable Node lastChild) {
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
