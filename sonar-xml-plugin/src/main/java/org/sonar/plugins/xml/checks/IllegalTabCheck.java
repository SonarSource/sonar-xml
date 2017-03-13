/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2017 SonarSource SA
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

import org.apache.commons.lang.StringUtils;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * RSPEC-105.
 * Perform check for tab.
 * @author Matthijs Galesloot
 */
@Rule(key = "IllegalTabCheck")
public class IllegalTabCheck extends AbstractXmlCheck {

  @RuleProperty(key = "markAll", description = "Mark all tab errors", defaultValue = "false")
  private boolean markAll;

  private boolean validationReady;

  /**
   * Find Illegal tabs in whitespace.
   */
  private void findIllegalTabs(Node node) {

    // check whitespace in the node
    for (Node sibling = node.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
      if (sibling.getNodeType() == Node.TEXT_NODE) {
        String text = sibling.getTextContent();
        if (StringUtils.isWhitespace(text) && StringUtils.contains(text, "\t")) {
          createNewViolation(getWebSourceCode().getLineForNode(sibling));
          // one violation for this node is enough
          break;
        }
      }
    }

    // check the child elements of the node
    for (Node child = node.getFirstChild(); !validationReady && child != null; child = child.getNextSibling()) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        findIllegalTabs(child);
      }
    }
  }

  private void createNewViolation(int lineNumber) {
    if (!markAll) {
      createViolation(lineNumber, "Tab characters found (this is the first occurrence)");
      validationReady = true;
    } else {
      createViolation(lineNumber, "Detect tab characters in your XML files.");
    }
  }

  @Override
  public void validate(XmlSourceCode xmlSourceCode) {
    setWebSourceCode(xmlSourceCode);

    validationReady = false;
    Document document = getWebSourceCode().getDocument(false);
    if (document.getDocumentElement() != null) {
      findIllegalTabs(document.getDocumentElement());
    }
  }

  public boolean isMarkAll() {
    return markAll;
  }

  public void setMarkAll(boolean markAll) {
    this.markAll = markAll;
  }
}
