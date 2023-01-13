/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2023 SonarSource SA
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.SafeDomParserFactory;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.XmlTextRange;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@Rule(key = CommentedOutCodeCheck.RULE_KEY)
public class CommentedOutCodeCheck extends SimpleXPathBasedCheck {

  public static final String RULE_KEY = "S125";

  private final XPathExpression commentsExpression = getXPathExpression("//comment()");

  private final Set<Node> visitedNodes = new HashSet<>();

  @Override
  public void scanFile(XmlFile file) {
    Charset charset = file.getInputFile().charset();

    for (Node comment : getComments(file)) {
      if (visitedNodes.contains(comment)) {
        // already reported in previous issue
        continue;
      }
      List<Node> siblingComments = getNextCommentSiblings(comment);
      checkCommentBlock(siblingComments, charset);
      visitedNodes.addAll(siblingComments);
    }
    // clear for next XML file
    visitedNodes.clear();
  }

  private void checkCommentBlock(List<Node> comments, Charset charset) {
    int numberComments = comments.size();
    for (int i = 0; i < numberComments; i++) {
      // considering all the combinations, starting from the biggest list possible and reducing from the top then
      List<Node> commentsGroup = comments.subList(i, numberComments);
      if (isParseableXml(commentsGroup, charset)) {
        XmlTextRange start = XmlFile.nodeLocation(commentsGroup.get(0));
        XmlTextRange end = XmlFile.nodeLocation(commentsGroup.get(commentsGroup.size() - 1));
        reportIssue(new XmlTextRange(start, end), "Remove this commented out code.", Collections.emptyList());
        break;
      }
    }
  }

  private List<Node> getComments(XmlFile file) {
    return evaluateAsList(commentsExpression, file.getDocument()).stream()
      .filter(comment -> comment.getTextContent().trim().startsWith("<"))
      .collect(Collectors.toList());
  }

  private static List<Node> getNextCommentSiblings(Node comment) {
    List<Node> results = new ArrayList<>();
    Node current = comment;
    while (current != null) {
      short nodeType = current.getNodeType();
      if (nodeType == Node.COMMENT_NODE) {
        results.add(current);
      } else if (nodeType != Node.TEXT_NODE || !isEmpty(current)) {
        // any other type discard collecting siblings
        break;
      }
      current = current.getNextSibling();
    }
    return results;
  }

  private static boolean isEmpty(Node current) {
    return current.getTextContent().trim().isEmpty();
  }

  private static boolean isParseableXml(List<Node> siblingComments, Charset charset) {
    String commentsAsSingleString = siblingComments.stream()
      .map(Node::getTextContent)
      .collect(Collectors.joining("\n"));

    try (ByteArrayInputStream stream = new ByteArrayInputStream(commentsAsSingleString.getBytes(charset))) {
      SafeDomParserFactory.createDocumentBuilder(false).parse(stream);
    } catch (IOException | SAXException e) {
      // swallow exception, we are just trying to parse to see if it could be some XML code
      return false;
    }
    return true;
  }
}
