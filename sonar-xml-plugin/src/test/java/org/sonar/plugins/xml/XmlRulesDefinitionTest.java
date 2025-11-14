/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.api.utils.Version;
import org.sonar.plugins.xml.checks.CheckList;

import static org.assertj.core.api.Assertions.assertThat;

class XmlRulesDefinitionTest {

  private static final Set<String> JAVA_DEPRECATED_KEYS = Stream.of(
    "S3281",
    "S3282",
    "S3439",
    "S3417",
    "S3419",
    "S3420",
    "S3421",
    "S3422",
    "S3423",
    "S3438",
    "S3355",
    "S3373",
    "S3374",
    "S3822"
    ).collect(Collectors.toSet());
  private static final Map<String, String> XML_DEPRECATED_KEYS = new HashMap<>();
  static {
    XML_DEPRECATED_KEYS.put("S105", "IllegalTabCheck");
    XML_DEPRECATED_KEYS.put("S1120", "IndentCheck");
    XML_DEPRECATED_KEYS.put("S2321", "NewlineCheck");
  }

  @Test
  void test() {
    XmlRulesDefinition rulesDefinition = new XmlRulesDefinition(SonarRuntimeImpl.forSonarLint(Version.create(9, 6)));
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("xml");

    assertThat(repository.name()).isEqualTo("Sonar");
    assertThat(repository.language()).isEqualTo("xml");
    assertThat(repository.rules()).hasSize(CheckList.getCheckClasses().size());

    RulesDefinition.Rule alertUseRule = repository.rule("S1120");
    assertThat(alertUseRule).isNotNull();
    assertThat(alertUseRule.name()).isEqualTo("Source code should be indented consistently");

    assertThat(repository.rules().stream().filter(Rule::template).map(Rule::key))
        .isNotEmpty()
        .containsOnly("XPathCheck", "S3417");

    for (Rule rule : repository.rules()) {
      for (RulesDefinition.Param param : rule.params()) {
        assertThat(param.description()).as("description for " + param.key()).isNotEmpty();
      }
      String key = rule.key();
      if (JAVA_DEPRECATED_KEYS.contains(key)) {
        assertThat(rule.deprecatedRuleKeys()).contains(RuleKey.of("java", key));
      } else if (XML_DEPRECATED_KEYS.containsKey(key)) {
        assertThat(rule.deprecatedRuleKeys()).contains(RuleKey.of("xml", XML_DEPRECATED_KEYS.get(key)));
      } else {
        assertThat(rule.deprecatedRuleKeys()).isEmpty();
      }
    }
  }

  @Test
  void test_security_standards() {
    XmlRulesDefinition rulesDefinition = new XmlRulesDefinition(SonarRuntimeImpl.forSonarLint(Version.create(9, 6)));
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("xml");

    RulesDefinition.Rule rule = repository.rule("S3281");

    assertThat(rule.securityStandards()).containsExactlyInAnyOrder("owaspTop10:a6", "owaspTop10-2021:a5");
  }

  @Test
  void test_security_standards_before_sq_9_3() {
    XmlRulesDefinition rulesDefinition = new XmlRulesDefinition(SonarRuntimeImpl.forSonarLint(Version.create(9, 2)));
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("xml");

    RulesDefinition.Rule rule = repository.rule("S3281");

    assertThat(rule.securityStandards()).containsExactlyInAnyOrder("owaspTop10:a6");
  }

}
