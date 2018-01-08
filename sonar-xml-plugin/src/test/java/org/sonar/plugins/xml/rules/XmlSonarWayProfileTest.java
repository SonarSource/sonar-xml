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

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.profiles.AnnotationProfileParser;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.language.Xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XmlSonarWayProfileTest {

  @Test
  public void should_create_sonar_way_profile() {
    ValidationMessages validation = ValidationMessages.create();

    RuleFinder ruleFinder = ruleFinder();
    XmlSonarWayProfile definition = new XmlSonarWayProfile(ruleFinder);
    RulesProfile profile = definition.createProfile(validation);

    assertThat(profile.getLanguage()).isEqualTo(Xml.KEY);
    assertThat(profile.getName()).isEqualTo(RulesProfile.SONAR_WAY_NAME);
    assertThat(profile.getActiveRulesByRepository(CheckRepository.REPOSITORY_KEY)).hasSize(1);
    assertThat(validation.hasErrors()).isFalse();
  }

  static RuleFinder ruleFinder() {
    return when(mock(RuleFinder.class).findByKey(Mockito.anyString(), Mockito.anyString())).thenAnswer(new Answer<Rule>() {
      public Rule answer(InvocationOnMock invocation) {
        Object[] arguments = invocation.getArguments();
        return Rule.create((String) arguments[0], (String) arguments[1], (String) arguments[1]);
      }
    }).getMock();
  }

}
