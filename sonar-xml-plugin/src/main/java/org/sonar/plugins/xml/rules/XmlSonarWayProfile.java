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

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Set;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.xml.checks.CheckRepository;
import org.sonar.plugins.xml.language.Xml;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Default XML profile.
 * 
 * @author Matthijs Galesloot
 */
public final class XmlSonarWayProfile extends ProfileDefinition {

  private final RuleFinder ruleFinder;

  public XmlSonarWayProfile(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages validation) {
    RulesProfile profile = RulesProfile.create(CheckRepository.SONAR_WAY_PROFILE_NAME, Xml.KEY);
    loadActiveKeysFromJsonProfile(profile);
    return profile;
  }

  private void loadActiveKeysFromJsonProfile(RulesProfile rulesProfile) {
    for (String ruleKey : activatedRuleKeys()) {
      Rule rule = ruleFinder.findByKey(CheckRepository.REPOSITORY_KEY, ruleKey);
      rulesProfile.activateRule(rule, null);
    }
  }

  public static Set<String> activatedRuleKeys() {
    URL profileUrl = XmlSonarWayProfile.class.getResource("/org/sonar/l10n/xml/rules/xml/Sonar_way_profile.json");
    try (Reader reader = new BufferedReader(new InputStreamReader(profileUrl.openStream(), UTF_8))) {
      Gson gson = new Gson();
      return gson.fromJson(reader, Profile.class).ruleKeys;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read " + profileUrl, e);
    }
  }

  private static class Profile {
    Set<String> ruleKeys;
  }

}
