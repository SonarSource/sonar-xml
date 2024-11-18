/*
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://www.sonarsource.com/legal/
 */
package org.sonar.plugins.xml;

import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.xml.checks.CheckList;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public final class XmlRulesDefinition implements RulesDefinition {

  private final SonarRuntime sonarRuntime;

  public XmlRulesDefinition(SonarRuntime sonarRuntime) {
    this.sonarRuntime = sonarRuntime;
  }

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(Xml.REPOSITORY_KEY, Xml.KEY).setName(Xml.REPOSITORY_NAME);

    RuleMetadataLoader ruleMetadataLoader = new RuleMetadataLoader(Xml.XML_RESOURCE_PATH, Xml.SONAR_WAY_PATH, sonarRuntime);

    // add the new checks
    ruleMetadataLoader.addRulesByAnnotatedClass(repository, CheckList.getCheckClasses());

    repository.rule("XPathCheck").setTemplate(true);
    repository.rule("S3417").setTemplate(true);
    repository.done();
  }
}
