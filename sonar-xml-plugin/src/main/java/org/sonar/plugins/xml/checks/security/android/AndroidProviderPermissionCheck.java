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
package org.sonar.plugins.xml.checks.security.android;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonarsource.analyzer.commons.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@Rule(key = "S6361")
public class AndroidProviderPermissionCheck extends AbstractAndroidManifestCheck {

  private static final String MESSAGE = "Make sure using a single permission for read and write is safe here.";
  private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
  private final XPathExpression xPathExpression = XPathBuilder
    .forExpression("/manifest/application/provider" +
      "[" +
      " (@n:permission and not(@n:readPermission) and not(@n:writePermission))" +
      "or" +
      " (@n:permission and @n:readPermission and not(@n:writePermission) and @n:permission = @n:readPermission)" +
      "or" +
      " (@n:permission and not(@n:readPermission) and @n:writePermission and @n:permission = @n:writePermission)" +
      "or" +
      " (not(@n:permission) and @n:readPermission and @n:writePermission and @n:readPermission = @n:writePermission)" +
      "]")
    .withNamespace("n", ANDROID_NS)
    .build();

  @Override
  protected final void scanAndroidManifest(XmlFile file) {
    evaluateAsList(xPathExpression, file.getDocument())
      .forEach(node -> {
        final NamedNodeMap attributes = node.getAttributes();
        final List<Node> nodes = Stream.of("permission", "readPermission", "writePermission")
          .map(s -> attributes.getNamedItemNS(ANDROID_NS, s))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
        reportIssue(
          XmlFile.nodeLocation(nodes.get(0)),
          MESSAGE,
          nodes.stream()
            .skip(1)
            .map(n -> new Secondary(n, null))
            .collect(Collectors.toList()));
      });
  }

}
