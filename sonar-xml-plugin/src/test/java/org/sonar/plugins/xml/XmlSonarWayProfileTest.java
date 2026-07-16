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

import org.junit.jupiter.api.Test;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.api.XmlProfileRegistrar;
import org.sonar.plugins.xml.checks.CheckList;

import java.util.List;

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

    @Test
    void profileWithRegistrarAddingAdditionalRules() {
        // Create a test registrar that adds custom rules to the built-in profile
        XmlProfileRegistrar testRegistrar = registrarContext -> {
            RuleKey customRule1 = RuleKey.of("custom-repo", "CUSTOM001");
            RuleKey customRule2 = RuleKey.of("another-repo", "ANOTHER001");
            registrarContext.registerDefaultQualityProfileRules(List.of(customRule1, customRule2));
        };

        // Create profile definition with the registrar
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
        new XmlSonarWayProfile(new XmlProfileRegistrar[]{testRegistrar}).define(context);
        BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("xml", "Sonar way");

        // Verify that the profile contains both default rules and additional rules from registrar
        assertThat(profile.rules()).hasSizeGreaterThan(2);
        assertThat(profile.rules()).extracting("repoKey").contains("xml", "custom-repo", "another-repo");
        assertThat(profile.rules()).extracting(BuiltInQualityProfilesDefinition.BuiltInActiveRule::ruleKey)
                .contains("S1135", "CUSTOM001", "ANOTHER001");
    }

}
