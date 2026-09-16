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
package org.sonar.plugins.xml;

import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.xml.api.XmlProfileRegistrar;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

import java.util.ArrayList;
import java.util.List;

public final class XmlSonarWayProfile implements BuiltInQualityProfilesDefinition {

  private final List<RuleKey> additionalRules = new ArrayList<>();

  public XmlSonarWayProfile() {
    this(new XmlProfileRegistrar[] {});
  }

  public XmlSonarWayProfile(XmlProfileRegistrar[] xmlProfileRegistrars) {
    for (XmlProfileRegistrar xmlProfileRegistrar : xmlProfileRegistrars) {
      xmlProfileRegistrar.register(additionalRules::addAll);
    }
  }

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile sonarWay = context.createBuiltInQualityProfile(Xml.SONAR_WAY_PROFILE_NAME, Xml.KEY);
    BuiltInQualityProfileJsonLoader.load(sonarWay, Xml.REPOSITORY_KEY, Xml.SONAR_WAY_PATH);
    additionalRules.forEach(ruleKey -> sonarWay.activateRule(ruleKey.repository(), ruleKey.rule()));
    sonarWay.done();
  }

}
