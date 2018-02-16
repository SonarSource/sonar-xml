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
package org.sonar.plugins.xml.rules;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.language.Xml;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

/**
 * Repository for XML rules.
 */
public final class XmlRulesDefinition implements RulesDefinition {

  private static final Set<String> TEMPLATE_RULES_KEY = Collections.unmodifiableSet(Stream.of("XPathCheck", "XmlSchemaCheck").collect(Collectors.toSet()));

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(CheckRepository.REPOSITORY_KEY, Xml.KEY)
      .setName(CheckRepository.REPOSITORY_NAME);

    // FIXME: with SonarQube 6.7, should use the sonar way profile location as extra parameter
    RuleMetadataLoader ruleMetadataLoader = new RuleMetadataLoader("org/sonar/l10n/xml/rules/xml");
    ruleMetadataLoader.addRulesByAnnotatedClass(repository, CheckRepository.getCheckClasses());

    for(NewRule rule : repository.rules()) {
      if (TEMPLATE_RULES_KEY.contains(rule.key())) {
        rule.setTemplate(true);
      }
    }

    repository.done();
  }
}
