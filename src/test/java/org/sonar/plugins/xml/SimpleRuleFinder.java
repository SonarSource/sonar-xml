/*
 * Sonar XML Plugin
 * Copyright (C) 2010 Matthijs Galesloot
 * dev@sonar.codehaus.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonar.plugins.xml;

import static junit.framework.Assert.assertNotNull;

import java.util.Collection;

import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.rules.RuleQuery;

public class SimpleRuleFinder implements RuleFinder {

  private final RulesProfile profile;

  public SimpleRuleFinder(RulesProfile profile) {
    this.profile = profile;
  }

  public Rule find(RuleQuery query) {
    return null;
  }

  public Collection<Rule> findAll(RuleQuery query) {
    return null;
  }

  public Rule findById(int arg0) {
    // TODO Auto-generated method stub
    return null;
  }

  public Rule findByKey(String repositoryKey, String key) {
    ActiveRule activeRule = profile.getActiveRuleByConfigKey(repositoryKey, key);
    assertNotNull(activeRule);
    return activeRule.getRule();
  }
}
