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

  public Rule findByKey(String repositoryKey, String key) {
    ActiveRule activeRule = profile.getActiveRuleByConfigKey(repositoryKey, key);
    assertNotNull(activeRule);
    return activeRule.getRule();
  }
}
