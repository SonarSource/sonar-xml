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
package org.sonar.plugins.xml;

import org.junit.Test;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.Version;
import org.sonar.plugins.xml.checks.NewXmlCheckList;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlSonarWayProfileTest {

  @Test
  public void should_create_sonar_way_profile() {
    SonarRuntime sonarRuntime = SonarRuntimeImpl.forSonarQube(Version.create(7, 3), SonarQubeSide.SERVER);
    ValidationMessages validation = ValidationMessages.create();

    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

    XmlSonarWayProfile definition = new XmlSonarWayProfile(sonarRuntime);
    definition.define(context);

    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile(Xml.KEY, Xml.SONAR_WAY_PROFILE_NAME);

    assertThat(profile.language()).isEqualTo(Xml.KEY);
    assertThat(profile.name()).isEqualTo(Xml.SONAR_WAY_PROFILE_NAME);
    assertThat(profile.rules().size()).isEqualTo(3);
    assertThat(profile.rules().size()).isLessThan(NewXmlCheckList.getCheckClasses().size());
    assertThat(validation.hasErrors()).isFalse();
  }
}
