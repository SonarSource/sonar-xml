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

package org.sonar.plugins.xml.rules;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.AnnotationRuleParser;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.xml.checks.AbstractPageCheck;
import org.sonar.plugins.xml.checks.XPathCheck;
import org.sonar.plugins.xml.checks.XmlSchemaCheck;
import org.sonar.plugins.xml.language.Xml;

/**
 * Repository for XML rules.
 *
 * @author Matthijs Galesloot
 * @since 1.0
 */
public final class XmlRulesRepository extends RuleRepository {

  private static final Class[] CHECK_CLASSES = new Class[] { XmlSchemaCheck.class, XPathCheck.class };
  private static final Logger LOG = LoggerFactory.getLogger(XmlRulesRepository.class);
  public static final String REPOSITORY_KEY = "Xml";
  public static final String REPOSITORY_NAME = "Xml";

  private static AbstractPageCheck createCheck(Class<? extends AbstractPageCheck> checkClass, ActiveRule activeRule) {

    try {
      AbstractPageCheck check = checkClass.newInstance();
      check.setRule(activeRule.getRule());
      if (activeRule.getActiveRuleParams() != null) {
        for (ActiveRuleParam param : activeRule.getActiveRuleParams()) {
          if ( !StringUtils.isEmpty(param.getValue())) {
            LOG.debug("Rule param " + param.getKey() + " = " + param.getValue());
            BeanUtils.setProperty(check, param.getRuleParam().getKey(), param.getValue());
          }
        }
      }

      return check;
    } catch (IllegalAccessException e) {
      throw new SonarException(e);
    } catch (InvocationTargetException e) {
      throw new SonarException(e);
    } catch (InstantiationException e) {
      throw new SonarException(e);
    }
  }

  public static List<AbstractPageCheck> createChecks(RulesProfile profile) {
    LOG.info("Loading checks for profile " + profile.getName());

    List<AbstractPageCheck> checks = new ArrayList<AbstractPageCheck>();

    for (ActiveRule activeRule : profile.getActiveRules()) {
      if (REPOSITORY_KEY.equals(activeRule.getRepositoryKey())) {
        Class<? extends AbstractPageCheck> clazz = findCheckClass(activeRule.getConfigKey());
        if (clazz != null) {
          checks.add(createCheck(clazz, activeRule));
        }
      }
    }

    return checks;
  }

  private static Class<? extends AbstractPageCheck> findCheckClass(String key) {
    for (Class<? extends AbstractPageCheck> clazz : CHECK_CLASSES) {
      org.sonar.check.Rule ruleAnnotation = clazz.getAnnotation(org.sonar.check.Rule.class);
      if (ruleAnnotation.key().equals(key)) {
        return clazz;
      }
    }

    return null;
  }

  private final AnnotationRuleParser annotationRuleParser;

  public XmlRulesRepository(AnnotationRuleParser annotationRuleParser) {
    super(REPOSITORY_KEY, Xml.KEY);
    setName(REPOSITORY_NAME);

    this.annotationRuleParser = annotationRuleParser;
  }

  @Override
  public List<Rule> createRules() {
    return annotationRuleParser.parse(getKey(), Arrays.asList(CHECK_CLASSES));
  }
}