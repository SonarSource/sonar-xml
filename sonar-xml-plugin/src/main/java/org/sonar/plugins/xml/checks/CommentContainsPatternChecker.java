/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
import javax.xml.xpath.XPathExpression;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;

public abstract class CommentContainsPatternChecker extends SimpleXPathBasedCheck {

  private final String pattern;
  private final String message;
  private final XPathExpression xPathExpression = getXPathExpression("//comment()");

  protected CommentContainsPatternChecker(String pattern, String message) {
    this.pattern = pattern.toLowerCase(Locale.ENGLISH);
    this.message = message;
  }

  @Override
  public final void scanFile(XmlFile file) {
    checkIfCommentContainsPattern(file);
  }

  private static boolean isLetterAround(String line, String pattern) {
    int start = line.indexOf(pattern);
    int end = start + pattern.length();

    boolean pre = start > 0 && Character.isLetter(line.charAt(start - 1));
    boolean post = end < line.length() - 1 && Character.isLetter(line.charAt(end));

    return pre || post;
  }

  private void checkIfCommentContainsPattern(XmlFile file) {
    evaluateAsList(xPathExpression, file.getDocument()).forEach(node -> {
      String comment = node.getNodeValue().toLowerCase(Locale.ENGLISH);
      if (comment.contains(pattern) && !isLetterAround(comment, pattern)) {
        reportIssue(node, message);
      }
    });
  }
}
