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
package org.sonar.plugins.xml.checks;

import java.util.Locale;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SonarXmlCheck;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CommentContainsPatternChecker {

  private final SonarXmlCheck check;
  private final String pattern;
  private final String message;

  public CommentContainsPatternChecker(SonarXmlCheck check, String pattern, String message) {
    this.check = check;
    this.pattern = pattern.toLowerCase(Locale.ENGLISH);
    this.message = message;
  }

  private static boolean isLetterAround(String line, String pattern) {
    int start = line.indexOf(pattern);
    int end = start + pattern.length();

    boolean pre = start > 0 && Character.isLetter(line.charAt(start - 1));
    boolean post = end < line.length() - 1 && Character.isLetter(line.charAt(end));

    return pre || post;
  }

  public void checkIfCommentContainsPattern(XmlFile file) {
    XPath xpath = XPathFactory.newInstance().newXPath();
    String expression = "//comment()";
    try {
      XPathExpression xPathExpression = xpath.compile(expression);
      NodeList nodes = (NodeList) xPathExpression.evaluate(file.getNamespaceUnawareDocument(), XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (null != node) {
          String comment = node.getNodeValue().toLowerCase(Locale.ENGLISH);
          if (comment.contains(pattern) && !isLetterAround(comment, pattern)) {
            check.reportIssue(node, message);
          }
        }
      }
    } catch (XPathExpressionException e) {
      throw new IllegalStateException("Failed to run XPath expression", e);
    }
  }
}
