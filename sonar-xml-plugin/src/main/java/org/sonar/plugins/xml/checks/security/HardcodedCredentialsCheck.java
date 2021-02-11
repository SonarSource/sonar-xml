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
package org.sonar.plugins.xml.checks.security;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.xpath.XPathExpression;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.xml.XPathBuilder;
import org.sonarsource.analyzer.commons.xml.XmlFile;
import org.sonarsource.analyzer.commons.xml.checks.SimpleXPathBasedCheck;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Rule(key = "S2068")
public class HardcodedCredentialsCheck extends SimpleXPathBasedCheck {

  private static final String DEFAULT_CREDENTIAL_WORDS = "password,passwd,pwd,passphrase";
  private static final Pattern VALID_CREDENTIAL_VALUES = Pattern.compile("[\\{$#]\\{.*", Pattern.DOTALL);

  @RuleProperty(
    key = "credentialWords",
    description = "Comma separated list of words identifying potential credentials",
    defaultValue = DEFAULT_CREDENTIAL_WORDS)
  public String credentialWords = DEFAULT_CREDENTIAL_WORDS;

  /**
   * Can not be pre-computed as it depends of the parameter
   */
  private XPathExpression anyCredentialAsTag = null;
  private XPathExpression getAnyCredentialAsTag() {
    if (anyCredentialAsTag == null) {
      anyCredentialAsTag = getXPathExpression(
        credentialWordsStream()
        .map(keyword -> "//" + keyword)
        .collect(Collectors.joining("|")));
    }
    return anyCredentialAsTag;
  }

  /**
   * Can not be pre-computed as it depends of the parameter
   */
  private XPathExpression anyCredentialAsAttribute = null;
  private XPathExpression getAnyCredentialAsAttribute() {
    if (anyCredentialAsAttribute == null) {
      anyCredentialAsAttribute = getXPathExpression(
        credentialWordsStream()
        .map(keyword -> "//*[@" + keyword + "]")
        .collect(Collectors.joining("|")));
    }
    return anyCredentialAsAttribute;
  }

  /**
   * Can not be pre-computed as it depends of the parameter
   */
  private List<Pattern> credentialPatterns = null;
  private List<Pattern> getCredentialPatterns() {
    if (credentialPatterns == null) {
      credentialPatterns = credentialWordsStream().map(Pattern::compile).collect(Collectors.toList());
    }
    return credentialPatterns;
  }

  /**
   * Can not be pre-computed as it depends of the parameter
   */
  private String[] cleanedCredentialWords = null;
  private Stream<String> credentialWordsStream() {
    if (cleanedCredentialWords == null) {
      cleanedCredentialWords = Stream.of(credentialWords.split(","))
        .map(String::trim)
        .collect(Collectors.toList())
        .toArray(new String[0]);
    }
    return Stream.of(cleanedCredentialWords);
  }

  @Override
  public void scanFile(XmlFile file) {
    Document namespaceUnawareDocument = file.getNamespaceUnawareDocument();
    checkTags(namespaceUnawareDocument);
    checkAttributes(namespaceUnawareDocument);
    checkSpecialCases(file);
  }

  private void checkTags(Document namespaceUnawareDocument) {
    for (Node tag : evaluateAsList(getAnyCredentialAsTag(), namespaceUnawareDocument)) {
      NodeList childNodes = tag.getChildNodes();
      if (childNodes.getLength() == 0) {
        checkAttribute(tag, "value", false);
      } else {
        singleChildTextNode(tag).ifPresent(candidate -> checkCredential(tag, candidate));
      }
    }
  }

  private static Optional<String> singleChildTextNode(Node node) {
    NodeList childNodes = node.getChildNodes();
    if (childNodes.getLength() == 1) {
      Node childNode = childNodes.item(0);
      if (childNode.getNodeType() == Node.TEXT_NODE) {
        return Optional.of(childNode.getTextContent());
      }
    }
    return Optional.empty();
  }

  private void checkAttributes(Document namespaceUnawareDocument) {
    for (Node node : evaluateAsList(getAnyCredentialAsAttribute(), namespaceUnawareDocument)) {
      credentialWordsStream().forEach(attribute -> checkAttribute(node, attribute, true));
    }
  }

  private void checkAttribute(Node node, String target, boolean reportOnAttribute) {
    NamedNodeMap attributes = node.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      Node attribute = attributes.item(i);
      if (attribute.getNodeName().contains(target)) {
        checkCredential(reportOnAttribute ? attribute : node, attribute.getTextContent());
        break;
      }
    }
  }

  private void checkCredential(Node credentialNode,  String candidate) {
    if (isValidCredential(candidate)) {
      return;
    }
    String credentialNodeName = credentialNode.getNodeName();
    getCredentialPatterns().stream()
      .filter(credentialPattern -> credentialPattern.matcher(credentialNodeName).matches())
      .findFirst()
      .ifPresent(credentialWord -> reportIssue(credentialNode, String.format("\"%s\" detected here, make sure this is not a hard-coded credential.", credentialWord)));
  }

  private static boolean isValidCredential(String candidate) {
    String trimmedCandidate = candidate.trim();
    return trimmedCandidate.isEmpty() || VALID_CREDENTIAL_VALUES.matcher(trimmedCandidate).matches();
  }

  private void checkSpecialCases(XmlFile file) {
    specialCases.forEach(specialCase -> specialCase.accept(file));
  }

  private final List<SpecialCase> specialCases = Arrays.asList(
    // FileZilla3
    new SpecialCase(
      "/FileZilla3/Servers/Server/Pass"
        + "|/FileZilla3/RecentServers/Server/Pass",
      HardcodedCredentialsCheck::getTextValueSafe,
      false),
    // Jenkins
    new SpecialCase(
      "/jenkins.plugins.publish_over_ssh.BapSshHostConfiguration/secretPassword"
        + "|/jenkins.plugins.publish_over_ssh.BapSshHostConfiguration/commonConfig/secretPassphrase"
        + "|/jenkins.plugins.publish_over_ssh.BapSshHostConfiguration/keyInfo/secretPassphrase",
      HardcodedCredentialsCheck::getTextValueSafe,
      false),
    // SonarQube
    new SpecialCase(
      "/SonarQubeAnalysisProperties/Property[@Name='sonar.login']"
        + "|project/properties/sonar.login",
      HardcodedCredentialsCheck::getTextValueSafe,
      false),
    // Spring Framework
    new SpecialCase(
      "/beans/bean/property/list/bean["
        + "@class='org.springframework.social.facebook.connect.FacebookConnectionFactory'"
        + " or @class='org.springframework.social.github.connect.GitHubConnectionFactory'"
        + " or @class='org.springframework.social.google.connect.GoogleConnectionFactory'"
        + " or @class='org.springframework.social.linkedin.connect.LinkedinConnectionFactory'"
        + " or @class='org.springframework.social.twitter.connect.TwitterConnectionFactory'"
        + "]/constructor-arg[2]",
      node -> getAttributeSafe(node, "value"),
      false),
    new SpecialCase(
      XPathBuilder.forExpression("/b:beans/f:config"
        + "|/b:beans/gh:config"
        + "|/b:beans/gg:config"
        + "|/b:beans/l:config"
        + "|/b:beans/t:config")
        .withNamespace("b", "http://www.springframework.org/schema/beans")
        .withNamespace("f", "http://www.springframework.org/schema/social/facebook")
        .withNamespace("gh", "http://www.springframework.org/schema/social/github")
        .withNamespace("gg", "http://www.springframework.org/schema/social/google")
        .withNamespace("l", "http://www.springframework.org/schema/social/linkedin")
        .withNamespace("t", "http://www.springframework.org/schema/social/twitter")
        .build(),
      node -> getAttributeSafe(node, "app-secret"),
      true
      ),
    // Teiid
    new SpecialCase(
      "/security-domain/authentication/login-module/module-option["
        + "@name='consumer-key' "
        + "or @name='consumer-secret'"
        + "or @name='access-key'"
        + "or @name='access-secret'"
        + "]",
      node -> getAttributeSafe(node, "value"),
      false)
    );

  private static Optional<Node> getTextValueSafe(Node node) {
    return Optional.ofNullable(node.getFirstChild());
  }

  private static Optional<Node> getAttributeSafe(Node node, String attributeName) {
    return node.hasAttributes() ? Optional.ofNullable(node.getAttributes().getNamedItem(attributeName)) : Optional.empty();
  }

  private class SpecialCase implements Consumer<XmlFile> {
    private final XPathExpression xpathExpression;
    private final Function<Node, Optional<Node>> credentialGetter;
    private final boolean usesNamespaces;
    private final boolean reportOnAttribute;

    private SpecialCase(String xPathExpression, Function<Node, Optional<Node>> credentialGetter, boolean reportOnAttribute) {
      this.xpathExpression = getXPathExpression(xPathExpression);
      this.usesNamespaces = false;
      this.credentialGetter = credentialGetter;
      this.reportOnAttribute = reportOnAttribute;
    }

    private SpecialCase(XPathExpression xPathExpression, Function<Node, Optional<Node>> credentialGetter, boolean reportOnAttribute) {
      this.xpathExpression = xPathExpression;
      this.usesNamespaces = true;
      this.credentialGetter = credentialGetter;
      this.reportOnAttribute = reportOnAttribute;
    }

    @Override
    public void accept(XmlFile file) {
      for (Node node : evaluateAsList(xpathExpression, usesNamespaces ? file.getNamespaceAwareDocument() : file.getNamespaceUnawareDocument())) {
        credentialGetter.apply(node).ifPresent(credentialNode -> {
          if (!isValidCredential(credentialNode.getNodeValue())) {
            reportIssue(reportOnAttribute ? credentialNode : node, "Make sure this is not a hard-coded credential.");
          }
        });
      }
    }
  }

}
