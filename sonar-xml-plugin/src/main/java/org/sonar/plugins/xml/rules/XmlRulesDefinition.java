/*
 * Copyright (C) 2010-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.xml.rules;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.language.Xml;
import org.sonar.squidbridge.annotations.AnnotationBasedRulesDefinition;

/**
 * Repository for XML rules.
 */
public final class XmlRulesDefinition implements RulesDefinition {

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(CheckRepository.REPOSITORY_KEY, Xml.KEY)
      .setName(CheckRepository.REPOSITORY_NAME);

    new AnnotationBasedRulesDefinition(repository, Xml.KEY).addRuleClasses(false, CheckRepository.getCheckClasses());

    repository.done();
  }

}
