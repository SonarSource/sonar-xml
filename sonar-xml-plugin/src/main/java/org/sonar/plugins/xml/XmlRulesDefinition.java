/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
