/*
 * SonarQube XML Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonarsource.xml;

import java.util.List;

import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.xml.api.XmlProfileRegistrar;

/**
 * Test implementation of XmlProfileRegistrar that adds a single rule to the default quality profile.
 */
public class TestProfileRegistrar implements XmlProfileRegistrar {

  @Override
  public void register(RegistrarContext registrarContext) {
    RuleKey ruleKey = RuleKey.of(TestRulesDefinition.REPOSITORY_KEY, TestRulesDefinition.RULE_KEY);
    registrarContext.registerDefaultQualityProfileRules(List.of(ruleKey));
  }
}
