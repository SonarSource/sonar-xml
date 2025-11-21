/*
 * SonarQube XML Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
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
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.xml;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.checks.CheckList;

import static org.assertj.core.api.Assertions.assertThat;

class XmlSonarWayProfileTest {

  @Test
  void should_create_sonar_way_profile() {
    ValidationMessages validation = ValidationMessages.create();

    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

    XmlSonarWayProfile definition = new XmlSonarWayProfile();
    definition.define(context);

    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile(Xml.KEY, Xml.SONAR_WAY_PROFILE_NAME);

    assertThat(profile.language()).isEqualTo(Xml.KEY);
    assertThat(profile.name()).isEqualTo(Xml.SONAR_WAY_PROFILE_NAME);
    assertThat(profile.rules()).hasSizeGreaterThan(10);
    assertThat(profile.rules()).hasSizeLessThan(CheckList.getCheckClasses().size());
    assertThat(validation.hasErrors()).isFalse();
  }
}
