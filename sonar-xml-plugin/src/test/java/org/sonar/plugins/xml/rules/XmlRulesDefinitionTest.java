/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.rules;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import org.sonar.plugins.xml.checks.CheckRepository;

public class XmlRulesDefinitionTest {

  @Test
  public void test() {
    XmlRulesDefinition rulesDefinition = new XmlRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("xml");

    assertThat(repository.name()).isEqualTo("SonarQube");
    assertThat(repository.language()).isEqualTo("xml");
    assertThat(repository.rules()).hasSize(CheckRepository.getChecks().size());

    RulesDefinition.Rule alertUseRule = repository.rule("IndentCheck");
    assertThat(alertUseRule).isNotNull();
    assertThat(alertUseRule.name()).isEqualTo("Source code should be indented consistently");

    for (Rule rule : repository.rules()) {
      for (RulesDefinition.Param param : rule.params()) {
        assertThat(param.description()).as("description for " + param.key()).isNotEmpty();
      }
    }
  }

}
